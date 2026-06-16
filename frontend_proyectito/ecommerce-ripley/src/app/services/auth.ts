import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, RegisterRequest, AuthResponse } from '../models/auth.model';
import { ApiResponse } from '../models/api-response';

import { UsuarioResponse } from '../models/user.model';
import { UserService } from './user.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // 2. Usamos la URL base global + el sufijo específico de este controlador
  private apiUrl = `${environment.apiUrl}/auth`;

  isAuthenticated = signal<boolean>(this.hasToken());
  currentUser = signal<UsuarioResponse | null>(null);
  userRole = signal<string>('');

  private userService = inject(UserService);

  constructor(private http: HttpClient) {
    if (this.isAuthenticated()) {
      this.extractRoleFromToken();
      this.loadUserProfile();
    }
  }


  login(credentials: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/signin`, credentials)
      .pipe(
        tap(response => this.handleAuthSuccess(response)),
        catchError(this.handleError)
      );
  }

  register(userData: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/signup`, userData)
      .pipe(
        tap(response => this.handleAuthSuccess(response)),
        catchError(this.handleError)
      );
  }

  logout(): void {
    localStorage.removeItem('token');

    this.isAuthenticated.set(false);
    this.currentUser.set(null);
    this.userRole.set('');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  private hasToken(): boolean {
    return !!localStorage.getItem('token');
  }

  loadUserProfile(): void {

    this.userService.getProfile().subscribe({
      next: (response) => {

        if (response.result && response.data) {
          this.currentUser.set(response.data);
          
        }
      },
      error: (error) => {
        console.error('❌ Error al obtener el perfil en Angular:', error);
      }
    });
  }
  /**
   * 3. Aquí imprimimos en consola y guardamos el token
   */
  private handleAuthSuccess(response: ApiResponse<AuthResponse>): void {


    if (response.data?.jwt) {
      localStorage.setItem('token', response.data.jwt);
      this.extractRoleFromToken();
      this.isAuthenticated.set(true);
      this.loadUserProfile(); // Cargar el perfil del usuario después de iniciar sesión
    }
  }

  private extractRoleFromToken(): void {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        let role = payload.role || payload.roles || payload.authorities || '';
        
        // Si el rol viene como [{ authority: 'ROLE_ADMIN' }] o similar (Spring Boot)
        if (Array.isArray(role) && role.length > 0) {
           role = role[0].authority || role[0].role || role[0];
        }

        // Si es un objeto, extrae la propiedad
        if (typeof role === 'object' && role !== null) {
           role = role.authority || role.role || '';
        }

        // Convertir a string y quitar el prefijo 'ROLE_' si existe
        role = String(role).replace('ROLE_', '').toUpperCase();
        
        this.userRole.set(role);
      } catch (e) {
        console.error('Error al decodificar el token:', e);
        this.userRole.set('');
      }
    }
  }

  private handleError(error: any) {
    console.error('Error de autenticación:', error);
    return throwError(() => error);
  }
}