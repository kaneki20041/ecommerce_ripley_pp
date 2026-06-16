import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SmartCartConfig {
  id?: number;
  active: boolean;
  upsellDiscountPercentage: number;
  maxProductsToShow: number;
  recommendationCriteria: string;
  minStockRequired: number;
  aiMetrics: string; // CSV: "USER_HISTORY,CTR_DATA,DISCOUNTS"
}

export interface SmartCartConfigResponse {
  result: boolean;
  message: string;
  data: SmartCartConfig;
}

@Injectable({
  providedIn: 'root'
})
export class SmartCartService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/admin/smart-cart/config`;

  getConfig(): Observable<SmartCartConfigResponse> {
    return this.http.get<SmartCartConfigResponse>(`${environment.apiUrl}/api/public/smart-cart/config`);
  }

  updateConfig(config: SmartCartConfig): Observable<SmartCartConfigResponse> {
    return this.http.put<SmartCartConfigResponse>(this.apiUrl, config);
  }
}
