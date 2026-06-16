import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { ModalComponent } from '../../../components/modal/modal';
import { OrderService, AdminOrderDetailResponse } from '../../../services/order.service';

interface OrderListRow {
  id: string;      // ID visual (ej: '#45')
  dbId: number;    // ID numérico de BD
  customer: string;
  total: number;
  date: string;
  status: 'pendiente' | 'proceso' | 'enviado' | 'completado' | 'cancelado';
  shippingMethod: string;
  storeName?: string;
}

@Component({
  selector: 'app-orders-management',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardSidebarComponent, DataTableComponent, ModalComponent, DecimalPipe, DatePipe],
  templateUrl: './orders-management.html',
  styleUrls: ['./orders-management.scss']
})
export class OrdersManagementComponent implements OnInit {
  private orderService = inject(OrderService);

  loading = signal(false);
  showDetailModal = signal(false);
  showReceiptModal = signal(false);
  selectedOrder = signal<AdminOrderDetailResponse | null>(null);
  trackingData = signal<any>(null);
  
  statusFilter = signal<string>('all');

  columns: TableColumn[] = [
    { key: 'id', label: 'ID Pedido', sortable: true, width: '100px' },
    { key: 'customer', label: 'Cliente', sortable: true },
    { key: 'shippingMethod', label: 'Tipo de Envío', type: 'badge', sortable: true },
    { key: 'total', label: 'Total', type: 'number', sortable: true },
    { key: 'date', label: 'Fecha', type: 'date', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge', sortable: true }
  ];

  ordersData = signal<OrderListRow[]>([]);

  actions: TableAction[] = [
    {
      label: 'Ver Detalle',
      icon: 'view',
      color: '#2196F3',
      onClick: (row) => this.viewDetail(row)
    },
    {
      label: 'Procesar',
      icon: 'play',
      color: '#4CAF50',
      onClick: (row) => this.updateOrderStatus(row, 'confirmed'),
      show: (row) => row.status === 'pendiente'
    },
    {
      label: 'Marcar Enviado / Listo',
      icon: 'shopping-bag',
      color: '#FF9800',
      onClick: (row) => this.updateOrderStatus(row, 'shipped'),
      show: (row) => row.status === 'proceso'
    },
    {
      label: 'Completar / Entregar',
      icon: 'check',
      color: '#4CAF50',
      onClick: (row) => this.updateOrderStatus(row, 'delivered'),
      show: (row) => row.status === 'enviado'
    },
    {
      label: 'Cancelar',
      icon: 'x',
      color: '#F44336',
      onClick: (row) => this.updateOrderStatus(row, 'cancel'),
      show: (row) => row.status === 'pendiente' || row.status === 'proceso'
    }
  ];

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);
    this.orderService.getAllAdminOrders().subscribe({
      next: (data) => {
        if (data) {
          const mappedOrders: OrderListRow[] = data.map(o => {
            // Mapear estado para mantener estilos del frontend
            let status: OrderListRow['status'] = 'pendiente';
            const s = o.orderStatus ? o.orderStatus.toUpperCase() : 'PENDING';
            if (s === 'CONFIRMED') status = 'proceso';
            else if (s === 'SHIPPED' || s === 'READY_FOR_PICKUP') status = 'enviado';
            else if (s === 'DELIVERED' || s === 'PICKED_UP' || s === 'COMPLETADO') status = 'completado';
            else if (s === 'CANCELLED') status = 'cancelado';
            else status = 'pendiente'; // PENDING / PLACED

            return {
              id: `#${o.id}`,
              dbId: o.id,
              customer: o.customerName || 'Cliente Ripley',
              total: o.totalDiscountedPrice || o.totalPrice || 0,
              date: o.orderDate ? new Date(o.orderDate).toISOString() : new Date().toISOString(),
              status: status,
              shippingMethod: o.shippingMethod || 'DOMICILIO',
              storeName: o.storeName
            };
          });
          
          // Ordenar de forma descendente por ID
          mappedOrders.sort((a, b) => b.dbId - a.dbId);
          this.ordersData.set(mappedOrders);
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar órdenes de administración:', err);
        this.loading.set(false);
      }
    });
  }

  viewDetail(row: OrderListRow): void {
    this.loading.set(true);
    this.trackingData.set(null); // Reset tracking
    this.orderService.getAdminOrderDetail(row.dbId).subscribe({
      next: (detail) => {
        this.selectedOrder.set(detail);
        
        // Si tiene tracking y está enviado o entregado, cargamos Shippo
        if ((detail.orderStatus === 'SHIPPED' || detail.orderStatus === 'DELIVERED') && detail.trackingNumber) {
          this.orderService.getOrderTracking(detail.id).subscribe({
            next: (track) => {
              this.trackingData.set(track);
              this.showDetailModal.set(true);
              this.loading.set(false);
            },
            error: (err) => {
              console.error('Error al obtener tracking para administrador:', err);
              this.showDetailModal.set(true);
              this.loading.set(false);
            }
          });
        } else {
          this.showDetailModal.set(true);
          this.loading.set(false);
        }
      },
      error: (err) => {
        console.error('Error al cargar el detalle del pedido:', err);
        this.loading.set(false);
      }
    });
  }

  closeDetailModal(): void {
    this.showDetailModal.set(false);
    this.selectedOrder.set(null);
    this.trackingData.set(null);
  }

  updateOrderStatus(order: OrderListRow, action: 'confirmed' | 'shipped' | 'delivered' | 'cancel'): void {
    this.loading.set(true);
    let obs$;
    
    if (action === 'confirmed') {
      obs$ = this.orderService.confirmOrder(order.dbId);
    } else if (action === 'shipped') {
      obs$ = this.orderService.shipOrder(order.dbId);
    } else if (action === 'delivered') {
      obs$ = this.orderService.deliverOrder(order.dbId);
    } else {
      obs$ = this.orderService.cancelOrder(order.dbId);
    }

    obs$.subscribe({
      next: () => {
        console.log(`Pedido ${order.id} actualizado con éxito a la acción: ${action}`);
        // Recargar la lista completa desde el backend para garantizar total consistencia
        this.loadOrders();
        // Si el detalle está abierto, cerrarlo para actualizar la vista
        this.closeDetailModal();
      },
      error: (err) => {
        console.error(`Error al actualizar pedido ${order.id} a la acción ${action}:`, err);
        this.loading.set(false);
      }
    });
  }

  getFilteredOrders(): OrderListRow[] {
    const filter = this.statusFilter();
    if (filter === 'all') return this.ordersData();
    return this.ordersData().filter(o => o.status === filter);
  }

  getDeliveredCount(): number {
    return this.ordersData().filter(o => o.status.toLowerCase() === 'completado').length;
  }

  printType = signal<'invoice' | 'receipt'>('invoice');

  canViewReceipt(order: AdminOrderDetailResponse | null): boolean {
    if (!order) return false;
    const status = (order.orderStatus || '').toLowerCase().trim();
    return ['enviado', 'completado', 'shipped', 'delivered'].includes(status);
  }

  printInvoice(order: AdminOrderDetailResponse | null): void {
    if (!order) return;
    this.printType.set('invoice');
    setTimeout(() => {
      window.print();
    }, 100);
  }

  viewReceipt(): void {
    this.printType.set('receipt');
    setTimeout(() => {
      window.print();
    }, 100);
  }

  getOrderTotal(order: AdminOrderDetailResponse | null): number {
    if (!order) return 0;
    return order.totalPrice;
  }

  getReceiptTotal(order: AdminOrderDetailResponse | null): number {
    if (!order?.products) return 0;
    return order.products.reduce((sum: number, p: any) => sum + (p.price * p.quantity), 0);
  }

  generateSKU(productName: string, orderId: number | undefined): string {
    if (!productName) return 'SKU-00000';
    // Generar un hash numérico simple a partir del nombre
    let hash = 0;
    for (let i = 0; i < productName.length; i++) {
      hash = productName.charCodeAt(i) + ((hash << 5) - hash);
    }
    const code = Math.abs(hash % 100000).toString().padStart(5, '0');
    // Extraer talla si existe en la descripción del producto (ej: "Talla: M")
    let size = '';
    const sizeMatch = productName.match(/Talla:\s*([A-Za-z0-9\-\/]+)/i);
    if (sizeMatch && sizeMatch[1]) {
      size = '-' + sizeMatch[1].toUpperCase();
    }
    return `RIP-${code}${size}`;
  }
}