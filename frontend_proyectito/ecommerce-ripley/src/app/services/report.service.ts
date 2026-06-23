import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ReportStatDto {
  title: string;
  value: string;
  icon: string;
  color: string;
}

export interface ApiResponse<T> {
  message: string;
  result: boolean;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = 'https://ecommerce-ripley-pp-1.onrender.com/api/admin/reports';

  constructor(private http: HttpClient) {}

  getReportStats(type: string, from: string, to: string): Observable<ApiResponse<ReportStatDto[]>> {
    let params = new HttpParams()
      .set('type', type)
      .set('from', from)
      .set('to', to);
      
    return this.http.get<ApiResponse<ReportStatDto[]>>(this.apiUrl, { params });
  }

  getReportDetails(type: string, from: string, to: string): Observable<ApiResponse<any[]>> {
    let params = new HttpParams()
      .set('type', type)
      .set('from', from)
      .set('to', to);
      
    return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/details`, { params });
  }
}
