import { inject, Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';
import { UsuarioResponse, AdminUsuarioResponse, AdminCreateUserRequest, AdminUpdateUserRequest } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/users`;
  private adminApiUrl = `${environment.apiUrl}/admin/gestion-usuarios`;

  getProfile(): Observable<ApiResponse<UsuarioResponse>> {
    return this.http.get<ApiResponse<UsuarioResponse>>(`${this.apiUrl}/profile`);
  }

  // Admin Methods
  getAllUsers(): Observable<ApiResponse<AdminUsuarioResponse[]>> {
    return this.http.get<ApiResponse<AdminUsuarioResponse[]>>(`${this.adminApiUrl}/all`);
  }

  createUserByAdmin(req: AdminCreateUserRequest): Observable<ApiResponse<AdminUsuarioResponse>> {
    return this.http.post<ApiResponse<AdminUsuarioResponse>>(`${this.adminApiUrl}/create`, req);
  }

  updateUserByAdmin(userId: number, req: AdminUpdateUserRequest): Observable<ApiResponse<AdminUsuarioResponse>> {
    return this.http.put<ApiResponse<AdminUsuarioResponse>>(`${this.adminApiUrl}/${userId}`, req);
  }

  deleteUser(userId: number): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(`${this.adminApiUrl}/${userId}`);
  }

  toggleUserStatus(userId: number): Observable<ApiResponse<string>> {
    return this.http.put<ApiResponse<string>>(`${this.adminApiUrl}/${userId}/status`, {});
  }
}
