import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CartService } from '../../../services/cart.service';
import { OrderService, OrderResponse } from '../../../services/order.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-checkout-success',
  standalone: true,
  imports: [CommonModule, RouterModule, DecimalPipe],
  templateUrl: './success.html',
  styleUrls: ['./success.scss']
})
export class CheckoutSuccessComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private cartService = inject(CartService);
  private orderService = inject(OrderService);

  orderId = signal<number | null>(null);
  orderData = signal<OrderResponse | null>(null);
  mpTransactionId = signal<string>('');
  loading = signal(false);

  ngOnInit(): void {
    // Generar un ID de transacción ficticio y muy realista de Mercado Pago
    const randomDigits = Math.floor(100000000 + Math.random() * 900000000);
    this.mpTransactionId.set(`MP-${randomDigits}`);

    // Leer el ID de orden desde los parámetros de la URL
    this.route.queryParams.subscribe(params => {
      const id = params['orderId'];
      if (id) {
        const parsedId = Number(id);
        this.orderId.set(parsedId);
        
        // Cargar los detalles reales de la orden guardada en el backend
        this.loadOrderDetails(parsedId);
      }
    });

    // ¡CRÍTICO!: Vaciar el carrito de compras tras el éxito de la transacción
    this.cartService.clearCart();
  }

  loadOrderDetails(id: number): void {
    this.loading.set(true);
    this.orderService.getOrderById(id).subscribe({
      next: (order) => {
        this.orderData.set(order);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar detalles de la orden en página éxito:', err);
        this.loading.set(false);
      }
    });
  }

  getFormattedDate(): string {
    const order = this.orderData();
    if (order && order.orderDate) {
      return new Date(order.orderDate).toLocaleDateString('es-PE', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    }
    return new Date().toLocaleDateString('es-PE', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  formatImageUrl(url: string): string {
    if (!url) return 'https://simple.ripley.com.pe/static/img/ripley-logo.svg';
    if (url.startsWith('/uploads') || url.startsWith('uploads')) {
      return `${environment.apiUrl}${url.startsWith('/') ? url : '/' + url}`;
    }
    return url;
  }

  getItemImage(item: any): string {
    if (!item || !item.product) return 'https://simple.ripley.com.pe/static/img/ripley-logo.svg';
    const p = item.product as any;

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
  }

  getItemUnitPrice(item: any): number {
    const qty = item.quantity || 1;
    const price = item.discountedPrice || item.price || 0;
    return price / qty;
  }
}
