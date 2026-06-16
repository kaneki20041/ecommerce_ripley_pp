import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Favorite {
  id?: number;
  product: any;
  addedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class FavoriteService {
  private apiUrl = `${environment.apiUrl}/api/favorites`;
  favoritesCount = signal<number>(0);

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  getFavorites(): Observable<any> {
    return this.http.get<any>(this.apiUrl, { headers: this.getHeaders() }).pipe(
      tap(res => {
        if (res.result) {
          this.favoritesCount.set(res.data.length);
        }
      })
    );
  }

  addFavorite(productId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${productId}`, {}, { headers: this.getHeaders() }).pipe(
      tap(res => {
        if (res.result) this.favoritesCount.update(c => c + 1);
      })
    );
  }

  removeFavorite(productId: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${productId}`, { headers: this.getHeaders() }).pipe(
      tap(res => {
        if (res.result) this.favoritesCount.update(c => Math.max(0, c - 1));
      })
    );
  }

  checkFavorite(productId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/check/${productId}`, { headers: this.getHeaders() });
  }
}
