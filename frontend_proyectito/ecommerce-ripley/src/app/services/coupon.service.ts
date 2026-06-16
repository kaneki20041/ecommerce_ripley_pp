import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Coupon {
  id?: number;
  name?: string;
  code: string;
  discount: number;
  type: string; // 'percentage' | 'fixed'
  minPurchase: number;
  startDate: string;
  endDate: string;
  status: string; // 'activo', 'inactivo', 'expirado'
  upsellDefault: boolean;
  usageCount: number;
  usageLimit: number;
}

export interface ApiResponse<T> {
  message: string;
  result: boolean;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class CouponService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/coupons`;

  validateCoupon(code: string, cartTotal: number): Observable<ApiResponse<Coupon>> {
    return this.http.get<ApiResponse<Coupon>>(`${this.apiUrl}/validate/${code}?cartTotal=${cartTotal}`);
  }

  getUpsellDefaultCoupon(): Observable<ApiResponse<Coupon>> {
    return this.http.get<ApiResponse<Coupon>>(`${this.apiUrl}/upsell-default`);
  }

  getMyCoupons(): Observable<ApiResponse<Coupon[]>> {
    return this.http.get<ApiResponse<Coupon[]>>(`${this.apiUrl}/my-coupons`);
  }

  // --- Admin Endpoints ---

  getAllAdminCoupons(): Observable<ApiResponse<Coupon[]>> {
    return this.http.get<ApiResponse<Coupon[]>>(`${environment.apiUrl}/api/admin/coupons/all`);
  }

  createCoupon(couponData: any): Observable<ApiResponse<Coupon>> {
    return this.http.post<ApiResponse<Coupon>>(`${environment.apiUrl}/api/admin/coupons/create`, couponData);
  }

  updateCoupon(id: number, couponData: any): Observable<ApiResponse<Coupon>> {
    return this.http.put<ApiResponse<Coupon>>(`${environment.apiUrl}/api/admin/coupons/${id}`, couponData);
  }

  deleteCoupon(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${environment.apiUrl}/api/admin/coupons/${id}`);
  }

  toggleCouponStatus(id: number): Observable<ApiResponse<Coupon>> {
    return this.http.patch<ApiResponse<Coupon>>(`${environment.apiUrl}/api/admin/coupons/${id}/toggle`, {});
  }
}
