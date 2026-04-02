import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { ModalComponent } from '../../../components/modal/modal';
import { ProductService } from '../../../services/product.service';
import { ProductAdminListResponse, CreateProductRequest, UpdateProductBasicRequest, UpdateVariantRequest } from '../../../models/product.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-products-management',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardSidebarComponent, DataTableComponent, ModalComponent],
  templateUrl: './products-management.html',
  styleUrls: ['./products-management.scss']
})
export class ProductsManagementComponent {
  private productService = inject(ProductService);

  loading = signal(false);
  showCreateModal = signal(false);
  showEditModal = signal(false);
  showDeactivateModal = signal(false);
  showActivateModal = signal(false);

  currentProductId: number = 0;
  currentVariantId: number = 0;

  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  categoryFilter = 'all';
  stockFilter = 'all';

  onPageChange(newPage: number): void {
    this.currentPage = newPage;
    this.cargarProductosDesdeServidor();
  }

  onPageSizeChange(): void {
    this.currentPage = 0; // Reset to first page
    this.cargarProductosDesdeServidor();
  }
  cargarProductosDesdeServidor(): void {
    this.loading.set(true);

    this.productService.getPaginatedAdminProducts(this.currentPage, this.pageSize)
      .subscribe({
        next: (response) => {
          if (response.result && response.data) {
            this.productsData = response.data.content;
            this.totalElements = response.data.totalElements;
            this.totalPages = response.data.totalPages;
            this.applyFilters();
          }
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Error al cargar productos:', error);
          this.loading.set(false);
        }
      });
  }
  availableTallas = ['4', '6', '8', '10', '12', '14', '16', 'S', 'M', 'L', 'XL'];
  newColorInput: string = '';

  productForm = {
    nombre: '',
    descripcion: '',
    categoria: '',
    genero: '',
    marca: '',
    material: '',
    colores: [] as string[],
    colorDetails: {} as Record<string, { stock: number; precio: number; precioAnterior: number; tallas: string[]; imagenes: string[]; fechaInicio: string; fechaFin: string }>,
    nuevo: false,
    destacado: false
  };

  columns: TableColumn[] = [
    { key: 'variantId', label: 'ID', width: '80px' },
    //{ key: 'imageUrl', label: 'Imagen', type: 'image' },
    { key: 'title', label: 'Producto', sortable: true },
    { key: 'color', label: 'Color', sortable: true },
    { key: 'size', label: 'Talla', sortable: true },
    { key: 'price', label: 'Precio', type: 'number', sortable: true },
    { key: 'stock', label: 'Stock', type: 'number', sortable: true },
    { key: 'estado', label: 'Estado', sortable: true }
  ];

  productsData: ProductAdminListResponse[] = [];
  filteredProductsData: ProductAdminListResponse[] = [];

  actions: TableAction[] = [
    { label: 'Editar', icon: 'edit', color: '#2196F3', onClick: (row) => this.openEditModal(row) },
    {
      label: 'Desactivar', icon: 'block', color: '#FF9800',
      show: (row: any) => row.estado === 'Activo' || row.estado === 'ACTIVO',
      onClick: (row) => {
        this.currentProductId = row.productId;
        this.currentVariantId = row.variantId;
        this.showDeactivateModal.set(true);
      }
    },
    {
      label: 'Activar', icon: 'check_circle', color: '#4CAF50',
      show: (row: any) => row.estado !== 'Activo' && row.estado !== 'ACTIVO',
      onClick: (row) => {
        this.currentProductId = row.productId;
        this.currentVariantId = row.variantId;
        this.showActivateModal.set(true);
      }
    }
  ];

  openEditModal(row: ProductAdminListResponse): void {
    this.currentProductId = row.productId;
    this.currentVariantId = row.variantId;
    this.loading.set(true);

    this.productService.getVariantByIdByAdmin(this.currentVariantId).subscribe({
      next: (res) => {
        if (res.result && res.data) {
          const v = res.data as any; // SingleVariantResponse de Java
          this.currentProductId = v.productId;
          this.productForm = {
            nombre: v.title || '',
            descripcion: v.description || '',
            categoria: v.categoria || row.categoria,
            genero: v.genero || '',
            marca: v.marca || '',
            material: v.material || '',
            colores: [v.color],
            colorDetails: {},
            nuevo: v.nuevo || false,
            destacado: v.destacado || false,
          };

          // Reconstruimos los detalles sólo para esa Variante (Color)
          this.productForm.colorDetails[v.color] = {
            precio: v.price || row.price,
            precioAnterior: v.descuentoprice || 0,
            stock: v.stock || row.stock || 0,
            tallas: v.size ? v.size.split(',') : [],
            imagenes: v.imageUrls || [],
            fechaInicio: this.formatDateForInput(v.fechaInicioDescuento),
            fechaFin: this.formatDateForInput(v.fechaFinDescuento)
          };
          this.loading.set(false);
          this.showEditModal.set(true);
        } else {
          alert('Error: ' + res.message);
          this.loading.set(false);
        }
      },
      error: (err) => {
        console.error('Error al cargar detalle del producto', err);
        alert('Error de red al cargar el producto para edición');
        this.loading.set(false);
      }
    });
  }

  getStockCount(status: string): number {
    if (status === 'disponible') return this.productsData.filter(p => p.stock > 10).length;
    if (status === 'bajo') return this.productsData.filter(p => p.stock > 0 && p.stock <= 10).length;
    if (status === 'agotado') return this.productsData.filter(p => p.stock === 0).length;
    return 0;
  }

  applyFilters() {
    this.filteredProductsData = this.productsData.filter(p => {
      const matchCategory = this.categoryFilter === 'all' || p.categoria === this.categoryFilter;
      let matchStock = true;
      if (this.stockFilter === 'disponible') matchStock = p.stock >= 10;
      if (this.stockFilter === 'bajo') matchStock = p.stock > 0 && p.stock < 10;
      if (this.stockFilter === 'agotado') matchStock = p.stock === 0;

      return matchCategory && matchStock;
    });
  }

  closeModal() {
    this.showCreateModal.set(false);
    this.showEditModal.set(false);
    this.showDeactivateModal.set(false);
    this.showActivateModal.set(false);
  }

  resetForm(): void {
    this.productForm = {
      nombre: '',
      descripcion: '',
      categoria: '',
      genero: '',
      marca: '',
      material: '',
      colores: [],
      colorDetails: {},
      nuevo: false,
      destacado: false
    };
  }

  capitalize(text: string): string {
    if (!text) return '';
    text = String(text).trim();
    return text.charAt(0).toUpperCase() + text.slice(1);
  }
  formatDateForJava(dateStr: string | undefined): string | undefined {
    if (!dateStr) return undefined;
    if (dateStr.length === 16) return dateStr + ':00';
    return dateStr;
  }


  formatDateForInput(dateData: any): string {
    if (!dateData) return '';

    if (typeof dateData === 'string') {
      return dateData.replace(' ', 'T').substring(0, 16);
    }

    if (Array.isArray(dateData) && dateData.length >= 5) {
      const year = dateData[0];
      const month = String(dateData[1]).padStart(2, '0');
      const day = String(dateData[2]).padStart(2, '0');
      const hour = String(dateData[3]).padStart(2, '0');
      const minute = String(dateData[4]).padStart(2, '0');
      return `${year}-${month}-${day}T${hour}:${minute}`;
    }

    return '';
  }
  // Genera el payload mapeado al nuevo CreateProductRequest -> coloresData
  private buildProductRequest(): CreateProductRequest {
    let totalStock = 0;
    let basePrice = 0;
    let baseDiscount = 0;
    const coloresDataPayload: any[] = [];

    const colors = this.productForm.colores;
    if (colors.length > 0) {
      const firstColor = this.productForm.colorDetails[colors[0]];
      basePrice = firstColor.precio;
      baseDiscount = firstColor.precioAnterior || 0;

      colors.forEach(col => {
        const det = this.productForm.colorDetails[col];
        totalStock += det.stock;

        coloresDataPayload.push({
          nombreColor: this.capitalize(col),
          imagenesColor: det.imagenes || [],
          sizesColor: det.tallas || [],
          price: det.precio || 0,
          descuentoprice: det.precioAnterior || 0,
          fechaInicioDescuento: this.formatDateForJava(det.fechaInicio),
          fechaFinDescuento: this.formatDateForJava(det.fechaFin)
        });
      });
    }

    return {
      title: this.capitalize(this.productForm.nombre),
      description: this.capitalize(this.productForm.descripcion),
      price: basePrice,
      descuentoprice: baseDiscount,
      stockTotal: totalStock,
      marca: this.capitalize(this.productForm.marca),
      material: this.capitalize(this.productForm.material),
      genero: this.capitalize(this.productForm.genero),
      categoria: this.capitalize(this.productForm.categoria),
      coloresData: coloresDataPayload,
      nuevo: this.productForm.nuevo,
      destacado: this.productForm.destacado
    };
  }

  createProduct(): void {
    const req = this.buildProductRequest();

    this.loading.set(true);
    this.productService.createProductByAdmin(req).subscribe({
      next: (res) => {
        if (res.result) {
          this.closeModal();
          this.resetForm();
          this.currentPage = 0; // Volvemos a la primera página para ver el nuevo
          this.cargarProductosDesdeServidor();
        } else {
          alert('Error: ' + res.message);
          this.loading.set(false);
        }
      },
      error: (err) => {
        console.error('Error al crear producto:', err);
        alert('Error de conexión al crear producto');
        this.loading.set(false);
      }
    });
  }

  openCreateModal(): void {
    this.resetForm(); // Limpiamos la basura de ediciones anteriores
    this.currentVariantId = 0;
    this.currentProductId = 0;
    this.showCreateModal.set(true); // Abrimos la ventana limpia
  }

  updateProduct(): void {
    if (this.productForm.colores.length === 0) {
      alert('Debe existir color en la variante.');
      return;
    }

    const colorKey = this.productForm.colores[0];
    const details = this.productForm.colorDetails[colorKey];

    // 1. Armamos el Request para la Variante
    const variantReq: UpdateVariantRequest = {
      color: this.capitalize(colorKey),
      size: details.tallas[0] || '',

      stock: details.stock,
      price: details.precio,
      descuentoprice: details.precioAnterior || 0,
      imageUrls: details.imagenes,
      fechaInicioDescuento: this.formatDateForJava(details.fechaInicio),
      fechaFinDescuento: this.formatDateForJava(details.fechaFin)
    };

    // 2. Armamos el Request para la Información Básica (Padre)
    const basicInfoReq: UpdateProductBasicRequest = {
      title: this.capitalize(this.productForm.nombre),
      description: this.capitalize(this.productForm.descripcion),
      marca: this.capitalize(this.productForm.marca),
      material: this.capitalize(this.productForm.material),
      genero: this.capitalize(this.productForm.genero),
      categoria: this.capitalize(this.productForm.categoria),
      nuevo: this.productForm.nuevo,
      destacado: this.productForm.destacado
    };

    this.loading.set(true);

    // 3. Preparamos las dos llamadas HTTP
    const updateVariant$ = this.productService.updateVariantByAdmin(this.currentVariantId, variantReq);
    const updateBasicInfo$ = this.productService.updateProductBasicInfo(this.currentProductId, basicInfoReq);

    // 4. Ejecutamos ambas peticiones en paralelo con forkJoin
    forkJoin([updateVariant$, updateBasicInfo$]).subscribe({
      next: ([variantRes, basicInfoRes]) => {
        // Validamos que ambas hayan sido exitosas (Spring devuelve result: true)
        if (variantRes.result && basicInfoRes.result) {
          this.closeModal();
          this.resetForm();
          this.cargarProductosDesdeServidor();
          alert('Producto y variante actualizados correctamente.');
        } else {
          // Si una falla, mostramos el mensaje de error de esa petición
          const errorMsg = !variantRes.result ? variantRes.message : basicInfoRes.message;
          alert('Error al actualizar: ' + errorMsg);
          this.loading.set(false);
        }
      },
      error: (err) => {
        console.error('Error durante la actualización múltiple:', err);
        alert('Error de conexión al guardar los cambios.');
        this.loading.set(false);
      }
    });
  }

  confirmDeactivate(): void {
    if (!this.currentVariantId && !this.currentProductId) return;

    this.loading.set(true);
    // Asume que usaremos currentVariantId (pero con fallback a currentProductId si es viejo)
    const targetId = this.currentVariantId || this.currentProductId;

    this.productService.toggleVariantStatusByAdmin(targetId).subscribe({
      next: (res: any) => {
        if (res.result) {
          const index = this.productsData.findIndex(p => p.variantId === targetId);
          if (index > -1) {
            this.productsData[index].estado = 'Inactivo';
            this.productsData = [...this.productsData];
            this.applyFilters();
          }
        } else {
          alert('No se pudo desactivar: ' + res.message);
        }
        this.loading.set(false);
      },
      error: (err: any) => {
        console.error('Error al desactivar:', err);
        alert('Error de red al desactivar');
        this.loading.set(false);
      }
    });

    this.showDeactivateModal.set(false);
  }

  confirmActivate(): void {
    if (!this.currentVariantId && !this.currentProductId) return;

    this.loading.set(true);
    const targetId = this.currentVariantId || this.currentProductId;

    this.productService.toggleVariantStatusByAdmin(targetId).subscribe({
      next: (res: any) => {
        if (res.result) {
          const index = this.productsData.findIndex(p => p.variantId === targetId);
          if (index > -1) {
            this.productsData[index].estado = 'Activo';
            this.productsData = [...this.productsData];
            this.applyFilters();
          }
        } else {
          alert('No se pudo activar: ' + res.message);
        }
        this.loading.set(false);
      },
      error: (err: any) => {
        console.error('Error al activar:', err);
        alert('Error de red al activar');
        this.loading.set(false);
      }
    });

    this.showActivateModal.set(false);
  }

  toggleTallaForColor(colorName: string, talla: string): void {
    const det = this.productForm.colorDetails[colorName];
    const index = det.tallas.indexOf(talla);
    if (index > -1) {
      det.tallas.splice(index, 1);
    } else {
      det.tallas.push(talla);
    }
  }

  isTallaSelectedForColor(colorName: string, talla: string): boolean {
    return this.productForm.colorDetails[colorName]?.tallas.includes(talla) || false;
  }

  addColor(): void {
    const color = this.capitalize(this.newColorInput);
    if (color && !this.productForm.colores.includes(color)) {
      this.productForm.colores.push(color);
      this.productForm.colorDetails[color] = {
        stock: 0,
        precio: 0,
        precioAnterior: 0,
        tallas: [],
        imagenes: [],
        fechaInicio: '',
        fechaFin: ''
      };
      this.newColorInput = ''; // clean input
    }
  }

  isColorSelected(colorName: string): boolean {
    return this.productForm.colores.includes(colorName);
  }

  toggleColor(colorName: string): void {
    const index = this.productForm.colores.indexOf(colorName);
    if (index > -1) {
      this.productForm.colores.splice(index, 1);
      delete this.productForm.colorDetails[colorName];
    } else {
      this.productForm.colores.push(colorName);
      this.productForm.colorDetails[colorName] = {
        stock: 0,
        precio: 0,
        precioAnterior: 0,
        tallas: [],
        imagenes: [],
        fechaInicio: '',
        fechaFin: ''
      };
    }
  }

  removeColorBlock(colorName: string): void {
    const index = this.productForm.colores.indexOf(colorName);
    if (index > -1) {
      this.productForm.colores.splice(index, 1);
      delete this.productForm.colorDetails[colorName];
    }
  }

  handleColorImageUpload(event: any, colorName: string): void {
    const details = this.productForm.colorDetails[colorName];
    if (event.target.files && event.target.files.length > 0) {
      for (let i = 0; i < event.target.files.length; i++) {
        if (details.imagenes.length < 4) {
          const reader = new FileReader();
          reader.onload = (e: any) => {
            details.imagenes.push(e.target.result);
          };
          reader.readAsDataURL(event.target.files[i]);
        }
      }
    }
  }

  removeColorImage(colorName: string, index: number): void {
    this.productForm.colorDetails[colorName].imagenes.splice(index, 1);
  }

  ngOnInit(): void {
    this.cargarProductosDesdeServidor();
  }
}