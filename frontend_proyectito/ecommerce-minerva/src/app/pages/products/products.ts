import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ProductService, ProductFilters } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { Product } from '../../models/product.model';
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
  products = signal<Product[]>([]);
  loading = signal(true);

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

  // Productos ordenados y paginados
  sortedProducts = computed(() => {
    const prods = this.products();
    const sorted = [...prods];

    switch (this.sortBy()) {
      case 'precio-asc':
        sorted.sort((a, b) => a.precio - b.precio);
        break;
      case 'precio-desc':
        sorted.sort((a, b) => b.precio - a.precio);
        break;
      case 'nuevos':
        sorted.sort((a, b) => (b.nuevo ? 1 : 0) - (a.nuevo ? 1 : 0));
        break;
      default:
        // relevancia - mostrar destacados primero
        sorted.sort((a, b) => (b.destacado ? 1 : 0) - (a.destacado ? 1 : 0));
    }

    return sorted;
  });

  paginatedProducts = computed(() => {
    const sorted = this.sortedProducts();
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return sorted.slice(start, end);
  });

  totalPages = computed(() => {
    return Math.ceil(this.sortedProducts().length / this.itemsPerPage);
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

    // Verificar si viene de una categoría específica
    this.route.params.subscribe(params => {
      if (params['categoria']) {
        this.selectedCategoria.set(params['categoria']);
      }
    });

    // Cargar productos
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set(true);

    const filters: ProductFilters = {
      categoria: this.selectedCategoria(),
      genero: this.selectedGenero(),
      precioMin: this.precioMin(),
      precioMax: this.precioMax(),
      tallas: this.selectedTallas(),
      busqueda: this.searchQuery(),
      nuevo: this.soloNuevos(),
      enOferta: this.soloOfertas()
    };

    this.productService.getFilteredProducts(filters).subscribe({
      next: (products) => {
        this.products.set(products);
        this.loading.set(false);
        this.currentPage.set(1); // Resetear a página 1
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
  }

  // Paginación
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
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

  quickAddToCart(product: Product): void {
    // Agregar con primera talla y color disponible
    const size = product.tallas[0];
    const color = product.colores[0].nombre;
    this.cartService.addToCart(product, 1, size, color);
  }

  // Mobile filters
  toggleFilters(): void {
    this.showFilters.set(!this.showFilters());
  }
}