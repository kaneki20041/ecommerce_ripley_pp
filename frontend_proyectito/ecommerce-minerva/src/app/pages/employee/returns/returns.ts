import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
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
}

@Component({
  selector: 'app-employee-returns',
  standalone: true,
  imports: [FormsModule, DashboardSidebarComponent, DataTableComponent, ModalComponent, DecimalPipe],
  templateUrl: './returns.html',
  styleUrls: ['./returns.scss'],
})
export class EmployeeReturnsComponent implements OnInit {
  loading = signal(false);
  showDetailModal = signal(false);
  
  selectedReturn = signal<Return | null>(null);

  statusFilter = signal<string>('all');

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
      status: 'pendiente'
    },
    {
      id: 2,
      orderId: '#1235',
      customer: 'Ana Martínez',
      product: 'Pantalón Gris L',
      reason: 'Producto defectuoso',
      amount: 65.00,
      requestDate: '2024-03-17',
      status: 'pendiente'
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
      label: 'Marcar Completado',
      icon: 'check',
      color: '#4CAF50',
      onClick: (row) => this.processReturn(row),
      show: (row) => row.status === 'aprobado'
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

  processReturn(returnItem: Return): void {
    console.log('Procesando devolución', returnItem);
  }

  getFilteredReturns(): Return[] {
    const filter = this.statusFilter();
    if (filter === 'all') return this.returnsData();
    return this.returnsData().filter(r => r.status === filter);
  }
}
