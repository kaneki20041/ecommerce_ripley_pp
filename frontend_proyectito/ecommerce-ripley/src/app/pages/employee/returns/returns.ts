import { Component, OnInit, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { ModalComponent } from '../../../components/modal/modal';
import { OrderRequestService } from '../../../services/order-request.service';
import { ToastService } from '../../../services/toast.service';

interface Return {
  id: number;
  orderId: string;
  customer: string;
  product: string;
  reason: string;
  amount: number;
  requestDate: string;
  type: string;
  status: 'pendiente' | 'aprobada' | 'rechazada' | string;
}

@Component({
  selector: 'app-employee-returns',
  standalone: true,
  imports: [FormsModule, DashboardSidebarComponent, DataTableComponent, ModalComponent, DecimalPipe],
  templateUrl: './returns.html',
  styleUrls: ['./returns.scss'],
})
export class EmployeeReturnsComponent implements OnInit {
  private orderRequestService = inject(OrderRequestService);
  private toast = inject(ToastService);

  loading = signal(false);
  showDetailModal = signal(false);
  
  selectedReturn = signal<Return | null>(null);

  statusFilter = signal<string>('all');

  columns: TableColumn[] = [
    { key: 'id', label: 'ID', width: '80px' },
    { key: 'orderId', label: 'Pedido', sortable: true },
    { key: 'customer', label: 'Cliente', sortable: true },
    { key: 'type', label: 'Tipo Solicitud', sortable: true },
    { key: 'amount', label: 'Monto Pedido', type: 'number', sortable: true },
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
          customer: req.user?.firstName + ' ' + req.user?.lastName,
          product: 'Pedido Completo',
          reason: req.reason,
          amount: req.order?.totalDiscountedPrice || req.order?.totalPrice || 0,
          requestDate: req.requestDate,
          type: req.type,
          status: req.status ? req.status.toLowerCase() : 'pendiente'
        }));
        this.returnsData.set(mapped.sort((a, b) => b.id - a.id));
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando solicitudes:', err);
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

  processReturn(returnItem: Return, newStatus: string): void {
    this.orderRequestService.updateRequestStatus(returnItem.id, newStatus).subscribe({
      next: () => {
        const label = newStatus === 'APROBADA' ? 'aprobada' : 'rechazada';
        this.toast.success(`Solicitud ${label}`, `La solicitud #${returnItem.id} fue ${label} correctamente.`);
        this.closeDetailModal();
        this.loadReturns();
      },
      error: (err) => {
        console.error('Error al actualizar solicitud', err);
        this.toast.error('Error al procesar', err.error?.message || 'Ocurrió un error al actualizar la solicitud.');
      }
    });
  }

  getFilteredReturns(): Return[] {
    const filter = this.statusFilter();
    if (filter === 'all') return this.returnsData();
    return this.returnsData().filter(r => r.status === filter);
  }
}
