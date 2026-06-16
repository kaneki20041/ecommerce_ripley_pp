import { Injectable, signal, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { ApiResponse, PaginatedResponse } from '../models/api-response';
import { Product, MOCK_PRODUCTS, CATEGORIAS, GENEROS, ProductAdminListResponse, CreateProductRequest, SingleVariantResponse, UpdateVariantRequest, UpdateProductBasicRequest, ProductCardResponse } from '../models/product.model';

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
   * Mapeador seguro para convertir el Product del Backend al Product del Frontend
   */
  private mapBackendProductToFrontend(backendProduct: any): Product {
    const colorMap = new Map<string, any>();
    const tallasSet = new Set<string>();
    let todasImagenes: string[] = [];

    if (backendProduct.variantes) {
      backendProduct.variantes.forEach((v: any) => {
        // Extraer valor de talla de forma segura (objeto Talla o string)
        const sizeVal = (v.talla && typeof v.talla === 'object') ? v.talla.valor : (v.size || v.talla || 'Única');
        tallasSet.add(sizeVal);

        // Extraer nombre de color de forma segura (objeto Color o string)
        const colorName = (v.color && typeof v.color === 'object') ? v.color.nombre : (v.color || 'Único');
        const colorHex = (v.color && typeof v.color === 'object') ? (v.color.hexCode || this.getColorHex(colorName)) : this.getColorHex(colorName);

        if (!colorMap.has(colorName)) {
          colorMap.set(colorName, { nombre: colorName, hexCode: colorHex, imagenes: [] });
        }

        // Añadir imágenes de este color
        if (v.imageUrls && v.imageUrls.length > 0) {
          const formattedUrls = v.imageUrls.map((url: string) => {
            if (url.startsWith('/uploads') || url.startsWith('uploads')) {
               return `${environment.apiUrl}${url.startsWith('/') ? url : '/' + url}`;
            }
            return url;
          });
          
          const colorObj = colorMap.get(colorName);
          formattedUrls.forEach((img: string) => {
            if (!colorObj.imagenes.includes(img)) {
              colorObj.imagenes.push(img);
            }
            if (!todasImagenes.includes(img)) {
              todasImagenes.push(img);
            }
          });
        }
      });
    }

    return {
      id: backendProduct.id,
      nombre: backendProduct.title,
      descripcion: backendProduct.description,
      precio: backendProduct.price,
      precioAnterior: backendProduct.discountedPrice,
      descuento: backendProduct.discountPercent,
      categoria: backendProduct.categoria?.name || backendProduct.categoria || 'Sin categoría',
      genero: backendProduct.genero,
      imagenes: todasImagenes.length > 0 ? todasImagenes : ['assets/images/placeholder.jpg'],
      tallas: Array.from(tallasSet),
      colores: Array.from(colorMap.values()),
      stock: backendProduct.variantes ? backendProduct.variantes.reduce((acc: number, v: any) => acc + (v.stock || 0), 0) : 0,
      nuevo: backendProduct.isNuevo || backendProduct.nuevo,
      destacado: backendProduct.isDestacado || backendProduct.destacado,
      rating: backendProduct.numRatings || 0,
      reviews: backendProduct.numRatings || 0,
      marca: backendProduct.marca,
      material: backendProduct.material,
      variantes: backendProduct.variantes
    };
  }

  /**
   * Obtener todos los productos reales del backend
   */
  getAllProducts(): Observable<Product[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/api/products/public/allproducts`).pipe(
      map(products => products.map(p => this.mapBackendProductToFrontend(p)))
    );
  }

  getProductById(id: number): Observable<Product | undefined> {
    return this.http.get<any>(`${environment.apiUrl}/api/products/public/products/id/${id}`).pipe(
      map(backendProduct => {
        if (!backendProduct) return undefined;
        return this.mapBackendProductToFrontend(backendProduct);
      })
    );
  }

  // Helper para mapear colores
  private getColorHex(colorName: string): string {
    const colorMap: { [key: string]: string } = {
      'blanco': '#FFFFFF', 'negro': '#000000', 'rojo': '#E53E3E', 'azul': '#3182CE',
      'azul marino': '#00205B', 'marino': '#00205B', 'verde': '#38A169', 'gris': '#718096',
      'gris oscuro': '#2D3748', 'amarillo': '#ECC94B', 'naranja': '#ED8936', 'rosa': '#ED64A6'
    };
    return colorMap[(colorName || '').toLowerCase().trim()] || '#CCCCCC';
  }

  /**
   * Obtener productos filtrados del backend/frontend dinámico
   */
  getFilteredProducts(filters: ProductFilters): Observable<Product[]> {
    return this.getAllProducts().pipe(
      map(products => {
        let filtered = [...products];

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

        return filtered;
      })
    );
  }

  /**
   * Obtener productos filtrados desde el endpoint público paginado
   */
  getFilteredProductsPublic(
    categoria?: string,
    genero?: string,
    isNuevo?: boolean,
    colors?: string[],
    sizes?: string[],
    minPrice?: number,
    maxPrice?: number,
    minDiscount?: number,
    sort?: string,
    stock?: string,
    pageNumber: number = 0,
    pageSize: number = 12
  ): Observable<ApiResponse<PaginatedResponse<ProductCardResponse>>> {
    let params = new HttpParams();
    if (categoria) params = params.set('categoria', categoria);
    if (genero) params = params.set('genero', genero);
    if (isNuevo !== undefined) params = params.set('isNuevo', isNuevo.toString());
    if (colors && colors.length > 0) colors.forEach(c => params = params.append('colors', c));
    if (sizes && sizes.length > 0) sizes.forEach(s => params = params.append('sizes', s));
    if (minPrice !== undefined) params = params.set('minPrice', minPrice.toString());
    if (maxPrice !== undefined) params = params.set('maxPrice', maxPrice.toString());
    if (minDiscount !== undefined) params = params.set('minDiscount', minDiscount.toString());
    if (sort) params = params.set('sort', sort);
    if (stock) params = params.set('stock', stock);
    params = params.set('pageNumber', pageNumber.toString());
    params = params.set('pageSize', pageSize.toString());

    return this.http.get<ApiResponse<PaginatedResponse<ProductCardResponse>>>(`${environment.apiUrl}/api/products/public/filter`, { params });
  }

  /**
   * Obtener productos destacados reales
   */
  getFeaturedProducts(pageNumber: number = 0, pageSize: number = 8): Observable<Product[]> {
    const params = new HttpParams().set('pageNumber', pageNumber.toString()).set('pageSize', pageSize.toString());
    return this.http.get<ApiResponse<PaginatedResponse<ProductCardResponse>>>(`${environment.apiUrl}/api/products/public/featured`, { params }).pipe(
      map(response => {
        // Mapeamos el DTO ProductCardResponse al Product del Frontend
        return response.data.content.map(card => this.mapCardToProduct(card));
      })
    );
  }

  /**
   * Obtener productos nuevos reales
   */
  getNewProducts(pageNumber: number = 0, pageSize: number = 8): Observable<Product[]> {
    const params = new HttpParams().set('pageNumber', pageNumber.toString()).set('pageSize', pageSize.toString());
    return this.http.get<ApiResponse<PaginatedResponse<ProductCardResponse>>>(`${environment.apiUrl}/api/products/public/new`, { params }).pipe(
      map(response => {
        return response.data.content.map(card => this.mapCardToProduct(card));
      })
    );
  }

  // Helper para mapear ProductCardResponse a Product
  private mapCardToProduct(card: ProductCardResponse): Product {
    let finalImageUrl = 'assets/images/placeholder.jpg';
    if (card.mainImageUrl) {
      if (card.mainImageUrl.startsWith('/uploads') || card.mainImageUrl.startsWith('uploads')) {
        finalImageUrl = `${environment.apiUrl}${card.mainImageUrl.startsWith('/') ? card.mainImageUrl : '/' + card.mainImageUrl}`;
      } else {
        finalImageUrl = card.mainImageUrl;
      }
    }

    return {
      id: card.id,
      nombre: card.title,
      descripcion: '',
      precio: card.price,
      precioAnterior: card.discountedPrice,
      descuento: card.discountPercent,
      categoria: 'General',
      genero: 'Unisex',
      imagenes: [finalImageUrl],
      tallas: [],
      colores: card.availableColors ? card.availableColors.map(c => ({ nombre: c, hexCode: this.getColorHex(c), imagenes: [] })) : [],
      stock: card.stock ?? 0,
      nuevo: card.nuevo,
      destacado: false,
      rating: 0,
      reviews: 0,
      marca: card.marca,
      material: '',
      variantes: []
    };
  }

  /**
   * Obtener productos relacionados reales
   */
  getRelatedProducts(productId: number, limit: number = 4): Observable<Product[]> {
    return this.getAllProducts().pipe(
      map(products => {
        const product = products.find(p => p.id === productId);
        if (!product) {
          return [];
        }
        return products
          .filter(p => p.id !== productId && p.categoria === product.categoria)
          .slice(0, limit);
      })
    );
  }

  /**
   * Buscar productos reales
   */
  searchProducts(query: string): Observable<Product[]> {
    const searchTerm = query.toLowerCase();
    return this.getAllProducts().pipe(
      map(products => products.filter(p =>
        p.nombre.toLowerCase().includes(searchTerm) ||
        p.descripcion.toLowerCase().includes(searchTerm) ||
        p.categoria.toLowerCase().includes(searchTerm)
      ))
    );
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

  /**
   * Obtener recomendaciones de IA basadas en el carrito (Backend)
   */
  getCartRecommendations(): Observable<ApiResponse<ProductCardResponse[]>> {
    let url = `${environment.apiUrl}/api/recomendaciones/carrito`;
    const ignoredStr = sessionStorage.getItem('ignoredCategories');
    if (ignoredStr) {
      try {
        const ignored: string[] = JSON.parse(ignoredStr);
        if (ignored.length > 0) {
          // Unir el arreglo con comas para el @RequestParam List<String>
          url += `?ignoredCategories=${ignored.join(',')}`;
        }
      } catch (e) {
        console.error('Error parsing ignoredCategories', e);
      }
    }
    return this.http.get<ApiResponse<ProductCardResponse[]>>(url);
  }
}