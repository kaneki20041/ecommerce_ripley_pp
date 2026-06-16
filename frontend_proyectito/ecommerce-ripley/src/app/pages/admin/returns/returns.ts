import { Component, OnInit, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { ModalComponent } from '../../../components/modal/modal';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { OrderRequestService } from '../../../services/order-request.service';
import { ToastService } from '../../../services/toast.service';
import { OrderService } from '../../../services/order.service';

interface Return {
  id: number;
  orderId: string;
  customer: string;
  type: string;
  reason: string;
  amount: number;
  requestDate: string;
  status: string;
}

@Component({
  selector: 'app-admin-returns',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardSidebarComponent, DataTableComponent, ModalComponent, DecimalPipe, DatePipe],
  templateUrl: './returns.html',
  styleUrls: ['./returns.scss']
})
export class AdminReturnsComponent implements OnInit {
  private orderRequestService = inject(OrderRequestService);
  private toast = inject(ToastService);
  private orderService = inject(OrderService);

  loading = signal(false);
  showDetailModal = signal(false);
  showRejectModal = signal(false);
  showReceiptModal = signal(false);
  receiptOrder = signal<any>(null);

  selectedReturn = signal<Return | null>(null);
  rejectReason = signal('');
  processingId = signal<number | null>(null);

  statusFilter = signal<string>('pendiente');

  columns: TableColumn[] = [
    { key: 'id', label: 'ID', width: '80px' },
    { key: 'orderId', label: 'Pedido', sortable: true },
    { key: 'customer', label: 'Cliente', sortable: true },
    { key: 'type', label: 'Tipo', sortable: true },
    { key: 'amount', label: 'Monto', type: 'number', sortable: true },
    { key: 'requestDate', label: 'Fecha', type: 'date', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge' }
  ];

  returnsData = signal<Return[]>([]);

  actions: TableAction[] = [
    {
      label: 'Ver Detalle',
      icon: 'view',
      color: '#2196F3',
      onClick: (row) => this.viewDetail(row)
    },
    {
      label: 'Aprobar',
      icon: 'check',
      color: '#4CAF50',
      onClick: (row) => this.approveReturn(row),
      show: (row) => row.status?.toLowerCase() === 'pendiente'
    },
    {
      label: 'Rechazar',
      icon: 'x',
      color: '#FF5252',
      onClick: (row) => this.openRejectModal(row),
      show: (row) => row.status?.toLowerCase() === 'pendiente'
    }
  ];

  ngOnInit(): void {
    this.loadReturns();
  }

  loadReturns(): void {
    this.loading.set(true);
    this.orderRequestService.getAllRequests().subscribe({
      next: (data) => {
        const mapped: Return[] = data.map((req: any) => ({
          id: req.id,
          orderId: `#${req.order?.id}`,
          customer: `${req.user?.firstName || ''} ${req.user?.lastName || ''}`.trim(),
          type: req.type,
          reason: req.reason,
          amount: req.order?.totalDiscountedPrice || req.order?.totalPrice || 0,
          requestDate: req.requestDate ? new Date(req.requestDate).toISOString() : new Date().toISOString(),
          status: req.status ? req.status.toLowerCase() : 'pendiente'
        }));
        this.returnsData.set(mapped.sort((a, b) => b.id - a.id));
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando solicitudes:', err);
        this.toast.error('Error al cargar', 'No se pudieron cargar las solicitudes.');
        this.loading.set(false);
      }
    });
  }

  viewDetail(returnItem: Return): void {
    this.selectedReturn.set(returnItem);
    this.showDetailModal.set(true);
  }

  closeDetailModal(): void {
    this.showDetailModal.set(false);
    this.selectedReturn.set(null);
  }

  approveReturn(returnItem: Return): void {
    this.processingId.set(returnItem.id);
    this.orderRequestService.updateRequestStatus(returnItem.id, 'APROBADA').subscribe({
      next: () => {
        this.toast.success('Solicitud aprobada', `La solicitud #${returnItem.id} fue aprobada correctamente.`);
        this.processingId.set(null);
        this.closeDetailModal();
        this.loadReturns();
      },
      error: (err) => {
        console.error('Error al aprobar:', err);
        this.toast.error('Error al aprobar', err.error?.message || 'Ocurrió un error al procesar la solicitud.');
        this.processingId.set(null);
      }
    });
  }

  openRejectModal(returnItem: Return): void {
    this.selectedReturn.set(returnItem);
    this.rejectReason.set('');
    this.showRejectModal.set(true);
  }

  closeRejectModal(): void {
    this.showRejectModal.set(false);
    this.selectedReturn.set(null);
    this.rejectReason.set('');
  }

  confirmReject(): void {
    const returnItem = this.selectedReturn();
    if (!returnItem) return;

    if (!this.rejectReason().trim()) {
      this.toast.warning('Campo requerido', 'Por favor, ingresa el motivo del rechazo.');
      return;
    }

    this.processingId.set(returnItem.id);
    this.orderRequestService.updateRequestStatus(returnItem.id, 'RECHAZADA').subscribe({
      next: () => {
        this.toast.success('Solicitud rechazada', `La solicitud #${returnItem.id} fue rechazada.`);
        this.processingId.set(null);
        this.closeRejectModal();
        this.loadReturns();
      },
      error: (err) => {
        console.error('Error al rechazar:', err);
        this.toast.error('Error al rechazar', err.error?.message || 'Ocurrió un error al procesar la solicitud.');
        this.processingId.set(null);
      }
    });
  }

  getFilteredReturns(): Return[] {
    const filter = this.statusFilter();
    if (filter === 'all') return this.returnsData();
    return this.returnsData().filter(r => (r.status || '').toLowerCase() === filter);
  }

  getPendingCount(): number {
    return this.returnsData().filter(r => (r.status || '').toLowerCase() === 'pendiente').length;
  }

  getApprovedCount(): number {
    return this.returnsData().filter(r => (r.status || '').toLowerCase() === 'aprobada').length;
  }

  viewReceipt(returnItem: Return): void {
    // Extract numeric ID from "#152" -> 152
    const orderId = parseInt((returnItem.orderId || '').replace('#', ''), 10);
    if (!orderId) return;
    this.orderService.getAdminOrderDetail(orderId).subscribe({
      next: (detail) => {
        this.receiptOrder.set({ ...detail, returnCustomer: returnItem.customer });
        this.showReceiptModal.set(true);
      },
      error: (err) => {
        console.error('Error cargando detalle del pedido:', err);
        this.toast.error('Error', 'No se pudo cargar el resumen del pedido.');
      }
    });
  }

  closeReceiptModal(): void {
    this.showReceiptModal.set(false);
    this.receiptOrder.set(null);
  }

  getReceiptTotal(order: any): number {
    if (!order?.products) return 0;
    return order.products.reduce((sum: number, p: any) => sum + (p.price * p.quantity), 0);
  }
}