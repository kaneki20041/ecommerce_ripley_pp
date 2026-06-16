import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ProductService, ProductFilters } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { Product, ProductCardResponse } from '../../models/product.model';
import { ProductCardComponent } from '../../components/product-card/product-card';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductCardComponent],
  templateUrl: './products.html',
  styleUrls: ['./products.scss']
})
export class ProductsComponent implements OnInit {
  // Señales
  products = signal<ProductCardResponse[]>([]);
  loading = signal(true);
  totalPagesServer = signal(1);
  totalElements = signal(0);

  // Filtros
  selectedCategoria = signal<string | undefined>(undefined);
  selectedGenero = signal<string | undefined>(undefined);
  precioMin = signal<number>(0);
  precioMax = signal<number>(500);
  selectedTallas = signal<string[]>([]);
  searchQuery = signal('');
  soloNuevos = signal(false);
  soloOfertas = signal(false);

  // Ordenamiento
  sortBy = signal<'relevancia' | 'precio-asc' | 'precio-desc' | 'nuevos'>('relevancia');

  // Paginación
  currentPage = signal(1);
  itemsPerPage = 12;

  // Listas para filtros
  categorias: string[] = [];
  generos: string[] = [];
  tallasDisponibles: string[] = ['6', '8', '10', '12', '14', '16', 'S', 'M', 'L', 'XL'];

  // Vista (grid/list)
  viewMode = signal<'grid' | 'list'>('grid');

  // Mostrar/Ocultar filtros en móvil
  showFilters = signal(false);

  // Productos (ahora vienen paginados y ordenados del servidor)
  paginatedProducts = computed(() => {
    return this.products();
  });

  totalPages = computed(() => {
    return this.totalPagesServer();
  });

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // Obtener categorías y géneros
    this.categorias = this.productService.getCategories();
    this.generos = this.productService.getGenders();

    // Verificar si viene de una categoría o género específica mediante Query Params
    this.route.queryParams.subscribe(params => {
      if (params['categoria']) {
        this.selectedCategoria.set(params['categoria']);
      }
      if (params['genero']) {
        this.selectedGenero.set(params['genero']);
      }
      
      // Aseguramos cargar productos con los nuevos filtros cada vez que cambien los query parameters
      this.loadProducts();
    });

    // Los productos se cargan dentro del subscribe de queryParams para asegurar que los filtros estén listos.
  }

  loadProducts(): void {
    this.loading.set(true);

    // Mapeamos sortBy a strings que el backend entienda, si es necesario, 
    // o simplemente enviamos el valor actual.
    // Usamos (currentPage - 1) porque Spring Data funciona con páginas 0-indexed.
    this.productService.getFilteredProductsPublic(
      this.selectedCategoria(),
      this.selectedGenero(),
      this.soloNuevos() ? true : undefined,
      undefined, // colors no definido en UI actual
      this.selectedTallas(),
      this.precioMin(),
      this.precioMax(),
      this.soloOfertas() ? 1 : undefined, // minDiscount
      this.sortBy(),
      undefined, // stock no mapeado actualmente
      this.currentPage() - 1,
      this.itemsPerPage
    ).subscribe({
      next: (response) => {
        if (response.result && response.data) {
          this.products.set(response.data.content);
          this.totalPagesServer.set(response.data.totalPages || 1);
          this.totalElements.set(response.data.totalElements || 0);
        } else {
          this.products.set([]);
          this.totalPagesServer.set(1);
          this.totalElements.set(0);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error cargando productos:', error);
        this.loading.set(false);
      }
    });
  }

  // Filtros
  onCategoriaChange(categoria: string): void {
    this.selectedCategoria.set(
      this.selectedCategoria() === categoria ? undefined : categoria
    );
    this.loadProducts();
  }

  onGeneroChange(genero: string): void {
    this.selectedGenero.set(
      this.selectedGenero() === genero ? undefined : genero
    );
    this.loadProducts();
  }

  onTallaToggle(talla: string): void {
    const current = this.selectedTallas();
    if (current.includes(talla)) {
      this.selectedTallas.set(current.filter(t => t !== talla));
    } else {
      this.selectedTallas.set([...current, talla]);
    }
    this.loadProducts();
  }

  onPrecioChange(): void {
    this.loadProducts();
  }

  onSearchChange(query: string): void {
    this.searchQuery.set(query);
    this.loadProducts();
  }

  toggleNuevos(): void {
    this.soloNuevos.set(!this.soloNuevos());
    this.loadProducts();
  }

  toggleOfertas(): void {
    this.soloOfertas.set(!this.soloOfertas());
    this.loadProducts();
  }

  clearFilters(): void {
    this.selectedCategoria.set(undefined);
    this.selectedGenero.set(undefined);
    this.precioMin.set(0);
    this.precioMax.set(500);
    this.selectedTallas.set([]);
    this.searchQuery.set('');
    this.soloNuevos.set(false);
    this.soloOfertas.set(false);
    this.loadProducts();
  }

  // Ordenamiento
  onSortChange(sort: 'relevancia' | 'precio-asc' | 'precio-desc' | 'nuevos'): void {
    this.sortBy.set(sort);
    this.loadProducts();
  }

  // Paginación
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
      this.loadProducts();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  // Vista
  toggleView(mode: 'grid' | 'list'): void {
    this.viewMode.set(mode);
  }

  // Navegación
  viewProduct(productId: number): void {
    this.router.navigate(['/producto', productId]);
  }

  // Carrito
  isInCart(productId: number): boolean {
    return this.cartService.isInCart(productId);
  }

  quickAddToCart(product: ProductCardResponse): void {
    // Adapter for cart - availableColors now contains color names (e.g. 'Negro', 'Blanco')
    const size = 'Única';
    const color = product.availableColors && product.availableColors.length > 0 ? product.availableColors[0] : '';
    this.cartService.addToCart(product as any, 1, size, color);
  }

  // Mobile filters
  toggleFilters(): void {
    this.showFilters.set(!this.showFilters());
  }
}