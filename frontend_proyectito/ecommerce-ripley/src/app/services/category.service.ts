import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response';

export interface CategoryRequest {
  name: string;
  padreCategoriaId?: number | null;
  level: number;
  activo: boolean;
}

export interface CategoryResponse {
  id: number;
  name: string;
  padreId?: number | null;
  padreName?: string | null;
  level: number;
  activo: boolean;
  subcategories: CategoryResponse[];
}

export interface CategoryFlatResponse {
  id: number;
  name: string;
  padreId?: number | null;
  padreName?: string | null;
  level: number;
  activo: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private http = inject(HttpClient);
  
  private publicApiUrl = `${environment.apiUrl}/api/public/categories`;
  private adminApiUrl = `${environment.apiUrl}/api/admin/categories`;

  constructor() { }

  // 1. Obtener árbol de categorías públicas (para el Navbar)
  getPublicCategoryTree(): Observable<ApiResponse<CategoryResponse[]>> {
    return this.http.get<ApiResponse<CategoryResponse[]>>(this.publicApiUrl);
  }

  // 2. Obtener árbol de categorías administrativo (con inactivas)
  getAdminCategoryTree(): Observable<ApiResponse<CategoryResponse[]>> {
    return this.http.get<ApiResponse<CategoryResponse[]>>(this.adminApiUrl);
  }

  // 3. Obtener lista plana de categorías (para selectores de padre)
  getAdminFlatCategories(): Observable<ApiResponse<CategoryFlatResponse[]>> {
    return this.http.get<ApiResponse<CategoryFlatResponse[]>>(`${this.adminApiUrl}/flat`);
  }

  // 4. Obtener una categoría por ID
  getCategoryById(id: number): Observable<ApiResponse<CategoryResponse>> {
    return this.http.get<ApiResponse<CategoryResponse>>(`${this.adminApiUrl}/${id}`);
  }

  // 5. Crear categoría
  createCategory(req: CategoryRequest): Observable<ApiResponse<CategoryResponse>> {
    return this.http.post<ApiResponse<CategoryResponse>>(this.adminApiUrl, req);
  }

  // 6. Editar categoría
  updateCategory(id: number, req: CategoryRequest): Observable<ApiResponse<CategoryResponse>> {
    return this.http.put<ApiResponse<CategoryResponse>>(`${this.adminApiUrl}/${id}`, req);
  }

  // 7. Toggle estado de categoría (soft-delete / reactivar)
  toggleCategoryStatus(id: number): Observable<ApiResponse<CategoryResponse>> {
    return this.http.put<ApiResponse<CategoryResponse>>(`${this.adminApiUrl}/toggle/${id}`, {});
  }
}
