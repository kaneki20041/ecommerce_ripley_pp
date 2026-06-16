import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CreateOrderRequestDto {
  orderId: number;
  type: string; // 'DEVOLUCION' | 'CANCELACION'
  reason: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderRequestService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api`;

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  // User Endpoints
  createRequest(req: CreateOrderRequestDto): Observable<any> {
    return this.http.post(`${this.apiUrl}/requests/create`, req, { headers: this.getAuthHeaders() });
  }

  getUserRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/requests/my-requests`, { headers: this.getAuthHeaders() });
  }

  // Admin Endpoints
  getAllRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/admin/requests/all`, { headers: this.getAuthHeaders() });
  }

  updateRequestStatus(requestId: number, status: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/admin/requests/${requestId}/status`, { status }, { headers: this.getAuthHeaders() });
  }
}
