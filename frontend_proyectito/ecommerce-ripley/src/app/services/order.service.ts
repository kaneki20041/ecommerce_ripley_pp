import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface ApiResponse<T> {
  message: string;
  result: boolean;
  data: T;
}

export interface AddressRequest {
  firstName: string;
  lastName: string;
  streetAddress: string;
  city: string;
  state: string;
  zipCode: string;
  celular: string;
}

export interface OrderItem {
  id: number;
  product: any;
  size: string;
  color: string;
  quantity: number;
  price: number;
  discountedPrice?: number;
}

export interface OrderResponse {
  id: number;
  orderId: string;
  usuario: any;
  orderItems: OrderItem[];
  orderDate: string;
  deliveryDate: string;
  shippingAddress: AddressRequest;
  paymentDetails?: any;
  totalPrice: number;
  totalDiscountedPrice: number;
  discounte: number;
  orderStatus: string;
  totalItem: number;
  createdAt: string;
  trackingNumber?: string;
  shippingLabelUrl?: string;
}

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/orders`;

  // Crear una nueva orden basada en el carrito actual y las opciones de despacho
  createOrder(orderRequest: { shippingAddress: AddressRequest; shippingMethod: string; storeName: string; couponCode?: string | null; }): Observable<OrderResponse> {
    return this.http.post<ApiResponse<OrderResponse>>(`${this.apiUrl}/`, orderRequest).pipe(
      map(res => res.data)
    );
  }

  // Crear preferencia de pago en Mercado Pago para una orden ya creada
  // Retorna el preferenceId para inicializar el Payment Brick en el frontend
  createPaymentPreference(orderId: number): Observable<{ preferenceId: string; initPoint: string; sandboxInitPoint: string }> {
    return this.http.post<ApiResponse<{ preferenceId: string; initPoint: string; sandboxInitPoint: string }>>(
      `${environment.apiUrl}/api/payments/create-preference`,
      { orderId }
    ).pipe(
      map(res => res.data)
    );
  }

  // Procesar el pago directamente con Mercado Pago (Payment Brick)
  processPayment(orderId: number, formData: any): Observable<any> {
    return this.http.post<ApiResponse<any>>(
      `${environment.apiUrl}/api/payments/process`,
      {
        orderId,
        token: formData.token,
        issuerId: formData.issuer_id,
        paymentMethodId: formData.payment_method_id,
        transactionAmount: formData.transaction_amount,
        installments: formData.installments,
        payer: formData.payer
      }
    ).pipe(
      map(res => res.data)
    );
  }

  // Obtener el historial de pedidos del usuario autenticado
  getUserOrders(): Observable<OrderResponse[]> {
    return this.http.get<ApiResponse<OrderResponse[]>>(`${this.apiUrl}/user`).pipe(
      map(res => res.data)
    );
  }

  // Obtener detalles de una orden específica por ID
  getOrderById(orderId: number): Observable<OrderResponse> {
    return this.http.get<ApiResponse<OrderResponse>>(`${this.apiUrl}/${orderId}`).pipe(
      map(res => res.data)
    );
  }

  // Obtener información de tracking en tiempo real desde Shippo para un pedido
  getOrderTracking(orderId: number): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/${orderId}/tracking`).pipe(
      map(res => res.data)
    );
  }

  // ============================================
  // GESTIÓN DE PEDIDOS DE ADMINISTRACIÓN (DTOS)
  // ============================================
  private adminApiUrl = `${environment.apiUrl}/api/admin/orders`;

  // Obtener todas las órdenes de la tienda (Admin / Empleado) usando el DTO de lista optimizado
  getAllAdminOrders(): Observable<AdminOrderListResponse[]> {
    return this.http.get<ApiResponse<AdminOrderListResponse[]>>(`${this.adminApiUrl}/`).pipe(
      map(res => res.data)
    );
  }

  // Obtener el detalle completo de una orden específica (para alimentar la ventana emergente/ojo)
  getAdminOrderDetail(orderId: number): Observable<AdminOrderDetailResponse> {
    return this.http.get<ApiResponse<AdminOrderDetailResponse>>(`${this.adminApiUrl}/${orderId}`).pipe(
      map(res => res.data)
    );
  }

  // Confirmar pedido (marcar como 'proceso') -> Retorna éxito vacío
  confirmOrder(orderId: number): Observable<any> {
    return this.http.put<ApiResponse<any>>(`${this.adminApiUrl}/${orderId}/confirmed`, {});
  }

  // Marcar pedido como enviado (marcar como 'enviado') -> Retorna éxito vacío
  shipOrder(orderId: number): Observable<any> {
    return this.http.put<ApiResponse<any>>(`${this.adminApiUrl}/${orderId}/ship`, {});
  }

  // Marcar pedido como entregado (marcar como 'completado') -> Retorna éxito vacío
  deliverOrder(orderId: number): Observable<any> {
    return this.http.put<ApiResponse<any>>(`${this.adminApiUrl}/${orderId}/deliver`, {});
  }

  // Cancelar pedido -> Retorna éxito vacío
  cancelOrder(orderId: number): Observable<any> {
    return this.http.put<ApiResponse<any>>(`${this.adminApiUrl}/${orderId}/cancel`, {});
  }

  // Eliminar pedido -> Retorna éxito vacío
  deleteOrder(orderId: number): Observable<any> {
    return this.http.put<ApiResponse<any>>(`${this.adminApiUrl}/${orderId}/delete`, {});
  }
}

// Interfaz para representar el DTO ligero de listado general de la tabla
export interface AdminOrderListResponse {
  id: number;
  orderId: string;
  customerName: string;
  totalPrice: number;
  totalDiscountedPrice: number;
  orderStatus: string;
  orderDate: string;
  shippingMethod?: string;
  storeName?: string;
}

// Interfaz para representar el DTO completo de detalle del pedido (para el modal)
export interface AdminOrderDetailResponse {
  id: number;
  orderId: string;
  customerName: string;
  email: string;
  phone: string;
  totalPrice: number;
  totalDiscountedPrice: number;
  orderStatus: string;
  shippingAddress: string;
  paymentMethod: string;
  orderDate: string;
  products: Array<{
    name: string;
    quantity: number;
    price: number;
  }>;
  trackingNumber?: string;
  shippingLabelUrl?: string;
  shippingMethod?: string;
  storeName?: string;
}
