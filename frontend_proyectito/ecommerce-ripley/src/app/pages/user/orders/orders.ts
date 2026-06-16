import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { OrderService, OrderResponse } from '../../../services/order.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterModule, DashboardSidebarComponent],
  templateUrl: './orders.html',
  styleUrls: ['./orders.scss']
})
export class OrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  
  orders = signal<OrderResponse[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    
    this.orderService.getUserOrders().subscribe({
      next: (data) => {
        this.orders.set(data || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar pedidos del usuario:', err);
        this.errorMessage.set('Hubo un problema al cargar tu historial de pedidos.');
        this.loading.set(false);
      }
    });
  }

  getOrderDate(dateStr: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('es-PE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  }

  formatImageUrl(url: string): string {
    if (!url) return 'https://simple.ripley.com.pe/static/img/ripley-logo.svg';
    if (url.startsWith('/uploads') || url.startsWith('uploads')) {
      return `${environment.apiUrl}${url.startsWith('/') ? url : '/' + url}`;
    }
    return url;
  }

  getOrderImages(order: OrderResponse): string[] {
    if (!order.orderItems) return [];
    return order.orderItems.map(item => {
      const p = item.product as any;
      if (!p) return 'https://simple.ripley.com.pe/static/img/ripley-logo.svg';
      
      let imgUrl = '';
      const selectedColor = item.color;

      // 1. Intentar buscar en variantes (Estructura de la Entidad JPA de Spring Boot)
      if (p.variantes && Array.isArray(p.variantes) && p.variantes.length > 0) {
        let variant = p.variantes.find((v: any) => v.color && v.color.nombre && selectedColor && v.color.nombre.toLowerCase() === selectedColor.toLowerCase());
        if (!variant) {
          variant = p.variantes[0];
        }
        if (variant && variant.imageUrls && variant.imageUrls.length > 0) {
          imgUrl = variant.imageUrls[0];
        }
      }

      // 2. Intentar buscar en colores del mock
      if (!imgUrl && p.colores && Array.isArray(p.colores)) {
        const colorObj = p.colores.find((c: any) => selectedColor && c.nombre && c.nombre.toLowerCase() === selectedColor.toLowerCase());
        if (colorObj && colorObj.imagenes && colorObj.imagenes.length > 0) {
          imgUrl = colorObj.imagenes[0];
        }
      }

      // 3. Fallbacks
      if (!imgUrl && p.imagenes && p.imagenes.length > 0) imgUrl = p.imagenes[0];
      if (!imgUrl && p.mainImageUrl) imgUrl = p.mainImageUrl;
      
      if (!imgUrl) return 'https://simple.ripley.com.pe/static/img/ripley-logo.svg';
      
      return this.formatImageUrl(imgUrl);
    });
  }
}