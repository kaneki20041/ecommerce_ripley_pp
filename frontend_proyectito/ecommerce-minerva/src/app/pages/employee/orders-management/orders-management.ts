import { Component, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { ModalComponent } from '../../../components/modal/modal';

interface Order {
  id: string;
  customer: string;
  email: string;
  phone: string;
  products: Array<{ name: string; quantity: number; price: number }>;
  total: number;
  date: string;
  status: 'pendiente' | 'proceso' | 'enviado' | 'completado' | 'cancelado';
  shippingAddress: string;
  paymentMethod: string;
}

@Component({
  selector: 'app-orders-management',
  standalone: true,
  imports: [FormsModule, DashboardSidebarComponent, DataTableComponent, ModalComponent, DecimalPipe],
  templateUrl: './orders-management.html',
  styleUrls: ['./orders-management.scss']
})
export class OrdersManagementComponent implements OnInit {
  loading = signal(false);
  showDetailModal = signal(false);
  selectedOrder = signal<Order | null>(null);
  
  statusFilter = signal<string>('all');

  columns: TableColumn[] = [
    { key: 'id', label: 'ID Pedido', sortable: true, width: '100px' },
    { key: 'customer', label: 'Cliente', sortable: true },
    { key: 'total', label: 'Total', type: 'number', sortable: true },
    { key: 'date', label: 'Fecha', type: 'date', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge', sortable: true }
  ];

  ordersData = signal<Order[]>([
    {
      id: '#1240',
      customer: 'Juan Pérez',
      email: 'juan@email.com',
      phone: '999888777',
      products: [
        { name: 'Camisa Escolar Blanca M', quantity: 2, price: 50 },
        { name: 'Pantalón Gris L', quantity: 1, price: 65 }
      ],
      total: 165.00,
      date: '2024-03-19',
      status: 'pendiente',
      shippingAddress: 'Av. Larco 1234, Miraflores',
      paymentMethod: 'Tarjeta'
    },
    {
      id: '#1241',
      customer: 'María García',
      email: 'maria@email.com',
      phone: '987654321',
      products: [
        { name: 'Falda Azul S', quantity: 1, price: 45 }
      ],
      total: 45.00,
      date: '2024-03-19',
      status: 'proceso',
      shippingAddress: 'Jr. Lima 567, Lima',
      paymentMethod: 'Efectivo'
    },
    {
      id: '#1242',
      customer: 'Carlos López',
      email: 'carlos@email.com',
      phone: '912345678',
      products: [
        { name: 'Zapatos Negros 40', quantity: 1, price: 120 }
      ],
      total: 120.00,
      date: '2024-03-18',
      status: 'enviado',
      shippingAddress: 'Av. Arequipa 890, San Isidro',
      paymentMethod: 'Yape'
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
      label: 'Procesar',
      icon: 'check',
      color: '#4CAF50',
      onClick: (row) => this.updateStatus(row, 'proceso'),
      show: (row) => row.status === 'pendiente'
    },
    {
      label: 'Marcar Enviado',
      icon: 'shopping-bag',
      color: '#FF9800',
      onClick: (row) => this.updateStatus(row, 'enviado'),
      show: (row) => row.status === 'proceso'
    },
    {
      label: 'Completar',
      icon: 'check',
      color: '#4CAF50',
      onClick: (row) => this.updateStatus(row, 'completado'),
      show: (row) => row.status === 'enviado'
    }
  ];

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);
    setTimeout(() => this.loading.set(false), 800);
  }

  viewDetail(order: Order): void {
    this.selectedOrder.set(order);
    this.showDetailModal.set(true);
  }

  closeDetailModal(): void {
    this.showDetailModal.set(false);
    this.selectedOrder.set(null);
  }

  updateStatus(order: Order, newStatus: Order['status']): void {
    const orders = this.ordersData();
    const index = orders.findIndex(o => o.id === order.id);
    if (index !== -1) {
      orders[index].status = newStatus;
      this.ordersData.set([...orders]);
    }
  }

  getFilteredOrders(): Order[] {
    const filter = this.statusFilter();
    if (filter === 'all') return this.ordersData();
    return this.ordersData().filter(o => o.status === filter);
  }

  printInvoice(order: Order): void {
    console.log('Imprimir factura:', order.id);
    window.print();
  }

  getOrderTotal(order: Order): number {
    return order.products.reduce((sum, p) => sum + (p.price * p.quantity), 0);
  }
}