import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { UpperCasePipe, DatePipe, DecimalPipe, CommonModule } from '@angular/common';
import { OrderService } from '../../../services/order.service';
import { OrderRequestService } from '../../../services/order-request.service';
import { ToastService } from '../../../services/toast.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, DashboardSidebarComponent, UpperCasePipe, DatePipe, DecimalPipe, RouterModule, FormsModule],
  templateUrl: './order-detail.html',
  styleUrls: ['./order-detail.scss']
})
export class OrderDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private orderService = inject(OrderService);
  private orderRequestService = inject(OrderRequestService);
  private toast = inject(ToastService);

  loading = signal(false);
  order = signal<any>(null);
  
  loadingTracking = signal(false);
  trackingData = signal<any>(null);

  // Modal State
  showRequestModal = signal(false);
  requestType = signal('');
  requestReason = signal('');
  submittingRequest = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    this.loadOrder(id);
  }

  loadOrder(id: string): void {
    this.loading.set(true);
    
    this.orderService.getOrderById(Number(id)).subscribe({
      next: (res) => {
        let fullAddress = '';
        if (res.shippingAddress) {
          const addr = res.shippingAddress;
          fullAddress = `${addr.streetAddress}, ${addr.city}, ${addr.state}`;
        }
        
        const productsMapped = (res.orderItems || []).map(item => {
          const p = item.product as any;
          const name = p.nombre || p.title || 'Producto Ripley';
          const colorDisplay = (item.color && item.color !== 'undefined' && item.color !== 'null') 
            ? item.color 
            : 'Sin especificar';
          const sizeDisplay = item.size || 'Sin especificar';
          return {
            name: `${name} (Color: ${colorDisplay} / Talla: ${sizeDisplay})`,
            quantity: item.quantity,
            price: item.price
          };
        });

        const statusLower = res.orderStatus ? res.orderStatus.toLowerCase() : 'pendiente';

        this.order.set({
          id: res.id,
          date: res.orderDate || res.createdAt,
          status: statusLower,
          total: res.totalDiscountedPrice || res.totalPrice,
          products: productsMapped,
          shippingAddress: fullAddress || 'Dirección de despacho no detallada',
          paymentMethod: 'Mercado Pago (Simulado)',
          trackingNumber: res.trackingNumber || ''
        });

        // Si el estado es enviado (SHIPPED) o completado (DELIVERED), y tiene tracking number, cargamos Shippo
        if ((res.orderStatus === 'SHIPPED' || res.orderStatus === 'DELIVERED') && res.trackingNumber) {
          this.loadTracking(res.id);
        }

        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar detalle del pedido:', err);
        this.loading.set(false);
      }
    });
  }

  loadTracking(orderId: number): void {
    this.loadingTracking.set(true);
    this.orderService.getOrderTracking(orderId).subscribe({
      next: (track) => {
        if (track) {
          this.trackingData.set(track);
        }
        this.loadingTracking.set(false);
      },
      error: (err) => {
        console.error('Error al obtener tracking de Shippo:', err);
        this.loadingTracking.set(false);
      }
    });
  }

  requestReturn(): void {
    if (this.order()?.status === 'pendiente' || this.order()?.status === 'placed') {
      this.requestType.set('CANCELACION');
      this.showRequestModal.set(true);
    } else if (this.order()?.status === 'delivered' || this.order()?.status === 'entregado') {
      this.requestType.set('DEVOLUCION');
      this.showRequestModal.set(true);
    } else {
      this.toast.warning('No disponible', 'El pedido no está en un estado válido para cancelación o devolución.');
    }
  }

  closeRequestModal(): void {
    this.showRequestModal.set(false);
    this.requestReason.set('');
  }

  submitRequest(): void {
    if (!this.requestReason().trim()) {
      this.toast.warning('Campo requerido', 'Por favor, ingresa el motivo de tu solicitud.');
      return;
    }

    const payload = {
      orderId: this.order().id,
      type: this.requestType(),
      reason: this.requestReason()
    };

    this.submittingRequest.set(true);
    this.orderRequestService.createRequest(payload).subscribe({
      next: () => {
        this.submittingRequest.set(false);
        this.closeRequestModal();
        this.toast.success('¡Solicitud enviada!', 'Tu solicitud está pendiente de revisión por nuestro equipo.');
      },
      error: (err) => {
        console.error('Error al crear solicitud:', err);
        this.submittingRequest.set(false);
        this.toast.error('Error al enviar', err.error?.message || 'Ocurrió un error. Por favor, inténtalo de nuevo.');
      }
    });
  }

  buyAgain(): void {
    console.log('Comprar de nuevo');
    // Implementar lógica de recompra agregando los productos al carrito
    this.router.navigate(['/carrito']);
  }

  downloadInvoice(): void {
    setTimeout(() => {
      window.print();
    }, 100);
  }

  canViewReceipt(): boolean {
    const o = this.order();
    if (!o) return false;
    const status = (o.status || '').toLowerCase().trim();
    return ['enviado', 'completado', 'shipped', 'delivered'].includes(status);
  }
}