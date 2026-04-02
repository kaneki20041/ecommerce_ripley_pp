import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { ModalComponent } from '../../../components/modal/modal';

interface Return {
  id: number;
  orderId: string;
  customer: string;
  product: string;
  reason: string;
  amount: number;
  requestDate: string;
  status: 'pendiente' | 'aprobado' | 'rechazado';
  employeeName?: string;
}

@Component({
  selector: 'app-admin-returns',
  standalone: true,
  imports: [FormsModule, DashboardSidebarComponent, DataTableComponent, ModalComponent],
  templateUrl: './returns.html',
  styleUrls: ['./returns.scss']
})
export class AdminReturnsComponent implements OnInit {
  loading = signal(false);
  showDetailModal = signal(false);
  showRejectModal = signal(false);
  
  selectedReturn = signal<Return | null>(null);
  rejectReason = signal('');

  statusFilter = signal<string>('pendiente');

  columns: TableColumn[] = [
    { key: 'id', label: 'ID', width: '80px' },
    { key: 'orderId', label: 'Pedido', sortable: true },
    { key: 'customer', label: 'Cliente', sortable: true },
    { key: 'product', label: 'Producto' },
    { key: 'amount', label: 'Monto', type: 'number', sortable: true },
    { key: 'requestDate', label: 'Fecha', type: 'date', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge' }
  ];

  returnsData = signal<Return[]>([
    {
      id: 1,
      orderId: '#1234',
      customer: 'Juan Pérez',
      product: 'Camisa Escolar Blanca M',
      reason: 'Talla incorrecta',
      amount: 50.00,
      requestDate: '2024-03-18',
      status: 'pendiente',
      employeeName: 'María García'
    },
    {
      id: 2,
      orderId: '#1235',
      customer: 'Ana Martínez',
      product: 'Pantalón Gris L',
      reason: 'Producto defectuoso',
      amount: 65.00,
      requestDate: '2024-03-17',
      status: 'pendiente',
      employeeName: 'Luis Torres'
    },
    {
      id: 3,
      orderId: '#1230',
      customer: 'Carlos López',
      product: 'Zapatos Negros 40',
      reason: 'No cumple expectativas',
      amount: 120.00,
      requestDate: '2024-03-15',
      status: 'aprobado'
    },
    {
      id: 4,
      orderId: '#1228',
      customer: 'María Silva',
      product: 'Falda Azul S',
      reason: 'Color diferente',
      amount: 45.00,
      requestDate: '2024-03-14',
      status: 'rechazado'
    }
  ]);

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
      show: (row) => row.status === 'pendiente'
    },
    {
      label: 'Rechazar',
      icon: 'x',
      color: '#FF5252',
      onClick: (row) => this.openRejectModal(row),
      show: (row) => row.status === 'pendiente'
    }
  ];

  ngOnInit(): void {
    this.loadReturns();
  }

  loadReturns(): void {
    this.loading.set(true);
    setTimeout(() => this.loading.set(false), 800);
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
    if (confirm(`¿Aprobar devolución de ${returnItem.customer}?`)) {
      const returns = this.returnsData();
      const index = returns.findIndex(r => r.id === returnItem.id);
      if (index !== -1) {
        returns[index].status = 'aprobado';
        this.returnsData.set([...returns]);
      }
    }
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

    const returns = this.returnsData();
    const index = returns.findIndex(r => r.id === returnItem.id);
    if (index !== -1) {
      returns[index].status = 'rechazado';
      this.returnsData.set([...returns]);
    }
    this.closeRejectModal();
  }

  getFilteredReturns(): Return[] {
    const filter = this.statusFilter();
    if (filter === 'all') return this.returnsData();
    return this.returnsData().filter(r => r.status === filter);
  }

  getPendingCount(): number {
    return this.returnsData().filter(r => r.status === 'pendiente').length;
  }

  getApprovedCount(): number {
    return this.returnsData().filter(r => r.status === 'aprobado').length;
  }
}