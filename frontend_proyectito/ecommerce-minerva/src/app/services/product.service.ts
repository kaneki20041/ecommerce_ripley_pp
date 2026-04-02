import { Injectable, signal, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { ApiResponse, PaginatedResponse } from '../models/api-response';
import { Product, MOCK_PRODUCTS, CATEGORIAS, GENEROS, ProductAdminListResponse, CreateProductRequest, SingleVariantResponse, UpdateVariantRequest, UpdateProductBasicRequest } from '../models/product.model';

export interface ProductFilters {
  categoria?: string;
  genero?: string;
  precioMin?: number;
  precioMax?: number;
  tallas?: string[];
  colores?: string[];
  busqueda?: string;
  nuevo?: boolean;
  enOferta?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  // Señales reactivas
  private productsSignal = signal<Product[]>(MOCK_PRODUCTS);
  products = this.productsSignal.asReadonly();

  private http = inject(HttpClient);
  private adminApiUrl = `${environment.apiUrl}/api/admin/products`;

  constructor() { }

  // Admin Endpoints
  createProductByAdmin(req: CreateProductRequest): Observable<ApiResponse<Product>> {
    return this.http.post<ApiResponse<Product>>(`${this.adminApiUrl}/create`, req);
  }

  toggleVariantStatusByAdmin(variantId: number): Observable<ApiResponse<string>> {
    return this.http.patch<ApiResponse<string>>(`${this.adminApiUrl}/variants/${variantId}/toggle-status`, {});
  }

  getPaginatedAdminProducts(page: number, size: number): Observable<ApiResponse<PaginatedResponse<ProductAdminListResponse>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PaginatedResponse<ProductAdminListResponse>>>(`${this.adminApiUrl}/all`, { params });
  }

  // updateProductByAdmin(productId: number, req: CreateProductRequest): Observable<ApiResponse<Product>> {
  //   return this.http.put<ApiResponse<Product>>(`${this.adminApiUrl}/${productId}/update`, req);
  // }

  updateProductBasicInfo(productId: number, data: UpdateProductBasicRequest): Observable<ApiResponse<Product>> {
    return this.http.put<ApiResponse<Product>>(`${this.adminApiUrl}/${productId}/basic-info`, data);
  }
  updateVariantByAdmin(variantId: number, req: UpdateVariantRequest): Observable<ApiResponse<SingleVariantResponse>> {
    return this.http.put<ApiResponse<SingleVariantResponse>>(`${this.adminApiUrl}/variants/${variantId}`, req);
  }

  getVariantByIdByAdmin(variantId: number): Observable<ApiResponse<SingleVariantResponse>> {
    return this.http.get<ApiResponse<SingleVariantResponse>>(`${this.adminApiUrl}/variants/${variantId}`);
  }

  /**
   * Obtener todos los productos
   */
  getAllProducts(): Observable<Product[]> {
    return of(MOCK_PRODUCTS); // Simular latencia de red
  }

  /**
   * Obtener un producto por ID
   */
  getProductById(id: number): Observable<Product | undefined> {
    const product = MOCK_PRODUCTS.find(p => p.id === id);
    return of(product);
  }

  /**
   * Obtener productos filtrados
   */
  getFilteredProducts(filters: ProductFilters): Observable<Product[]> {
    let filtered = [...MOCK_PRODUCTS];

    // Filtrar por categoría
    if (filters.categoria) {
      filtered = filtered.filter(p => p.categoria === filters.categoria);
    }

    // Filtrar por género
    if (filters.genero) {
      filtered = filtered.filter(p => p.genero === filters.genero || p.genero === 'Unisex');
    }

    // Filtrar por precio
    if (filters.precioMin !== undefined) {
      filtered = filtered.filter(p => p.precio >= filters.precioMin!);
    }
    if (filters.precioMax !== undefined) {
      filtered = filtered.filter(p => p.precio <= filters.precioMax!);
    }

    // Filtrar por tallas
    if (filters.tallas && filters.tallas.length > 0) {
      filtered = filtered.filter(p =>
        p.tallas.some(t => filters.tallas!.includes(t))
      );
    }

    // Filtrar por búsqueda
    if (filters.busqueda) {
      const busqueda = filters.busqueda.toLowerCase();
      filtered = filtered.filter(p =>
        p.nombre.toLowerCase().includes(busqueda) ||
        p.descripcion.toLowerCase().includes(busqueda) ||
        p.categoria.toLowerCase().includes(busqueda)
      );
    }

    // Filtrar solo nuevos
    if (filters.nuevo) {
      filtered = filtered.filter(p => p.nuevo);
    }

    // Filtrar solo en oferta
    if (filters.enOferta) {
      filtered = filtered.filter(p => p.descuento && p.descuento > 0);
    }

    return of(filtered);
  }

  /**
   * Obtener productos destacados
   */
  getFeaturedProducts(): Observable<Product[]> {
    const featured = MOCK_PRODUCTS.filter(p => p.destacado);
    return of(featured);
  }

  /**
   * Obtener productos nuevos
   */
  getNewProducts(): Observable<Product[]> {
    const newProducts = MOCK_PRODUCTS.filter(p => p.nuevo);
    return of(newProducts);
  }

  /**
   * Obtener productos relacionados (misma categoría)
   */
  getRelatedProducts(productId: number, limit: number = 4): Observable<Product[]> {
    const product = MOCK_PRODUCTS.find(p => p.id === productId);
    if (!product) {
      return of([]);
    }

    const related = MOCK_PRODUCTS
      .filter(p =>
        p.id !== productId &&
        p.categoria === product.categoria
      )
      .slice(0, limit);

    return of(related);
  }

  /**
   * Buscar productos
   */
  searchProducts(query: string): Observable<Product[]> {
    const searchTerm = query.toLowerCase();
    const results = MOCK_PRODUCTS.filter(p =>
      p.nombre.toLowerCase().includes(searchTerm) ||
      p.descripcion.toLowerCase().includes(searchTerm) ||
      p.categoria.toLowerCase().includes(searchTerm)
    );
    return of(results);
  }

  /**
   * Obtener categorías disponibles
   */
  getCategories(): string[] {
    return CATEGORIAS;
  }

  /**
   * Obtener géneros disponibles
   */
  getGenders(): string[] {
    return GENEROS;
  }

  /**
   * Obtener rango de precios
   */
  getPriceRange(): { min: number; max: number } {
    const precios = MOCK_PRODUCTS.map(p => p.precio);
    return {
      min: Math.min(...precios),
      max: Math.max(...precios)
    };
  }
}