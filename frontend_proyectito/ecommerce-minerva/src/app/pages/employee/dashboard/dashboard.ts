import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { StatsCardComponent } from '../../../components/stats-card/stats-card';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, DashboardSidebarComponent, StatsCardComponent, DataTableComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class DashboardComponent implements OnInit {
  loading = signal(false);

  // Stats data (3 cards para employee, no 4)
  stats = [
    { title: 'Productos Activos', value: '856', icon: 'box', color: '#FFD93D', trend: { value: 5.2, isUp: true } },
    { title: 'Pedidos Pendientes', value: '23', icon: 'shopping-bag', color: '#FF5252', trend: { value: 12.0, isUp: false } },
    { title: 'Stock Bajo', value: '12', icon: 'trending-up', color: '#FF9800', trend: { value: 2.5, isUp: true } }
  ];

  // Pending orders columns
  pendingOrdersColumns: TableColumn[] = [
    { key: 'id', label: 'ID', width: '100px' },
    { key: 'customer', label: 'Cliente', sortable: true },
    { key: 'total', label: 'Total', type: 'number', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge' },
    { key: 'date', label: 'Fecha', type: 'date', sortable: true }
  ];

  pendingOrdersData = [
    { id: '#1240', customer: 'Juan Pérez', total: 250.50, status: 'pendiente', date: '2024-03-19' },
    { id: '#1241', customer: 'María García', total: 180.00, status: 'proceso', date: '2024-03-19' },
    { id: '#1242', customer: 'Carlos López', total: 420.75, status: 'pendiente', date: '2024-03-18' },
    { id: '#1243', customer: 'Ana Martínez', total: 95.20, status: 'proceso', date: '2024-03-18' }
  ];

  ordersActions: TableAction[] = [
    {
      label: 'Procesar',
      icon: 'check',
      color: '#4CAF50',
      onClick: (row) => this.processOrder(row),
      show: (row) => row.status === 'pendiente'
    },
    {
      label: 'Ver',
      icon: 'view',
      color: '#2196F3',
      onClick: (row) => this.viewOrder(row)
    }
  ];

  // Low stock products
  lowStockProducts = [
    { name: 'Camisa Escolar Blanca M', stock: 3, category: 'Uniformes' },
    { name: 'Pantalón Gris L', stock: 5, category: 'Uniformes' },
    { name: 'Falda Azul Marino S', stock: 2, category: 'Uniformes' },
    { name: 'Zapatos Negros 38', stock: 4, category: 'Calzado' },
    { name: 'Buzo Deportivo XL', stock: 6, category: 'Deportivo' }
  ];

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading.set(true);
    setTimeout(() => {
      this.loading.set(false);
    }, 800);
  }

  processOrder(row: any): void {
    console.log('Procesar pedido:', row);
    // Cambiar estado a "proceso"
    const index = this.pendingOrdersData.findIndex(o => o.id === row.id);
    if (index !== -1) {
      this.pendingOrdersData[index].status = 'proceso';
      this.pendingOrdersData = [...this.pendingOrdersData];
    }
  }

  viewOrder(row: any): void {
    console.log('Ver pedido:', row);
  }

  refreshData(): void {
    this.loadDashboardData();
  }
}