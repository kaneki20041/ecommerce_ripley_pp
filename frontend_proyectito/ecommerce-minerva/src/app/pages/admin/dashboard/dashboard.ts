import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { StatsCardComponent } from '../../../components/stats-card/stats-card';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, DashboardSidebarComponent, StatsCardComponent, DataTableComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class AdminDashboardComponent implements OnInit {
  loading = signal(false);
  sidebarCollapsed = false;

  // Stats data
  stats = [
    { title: 'Ventas Totales', value: 'S/ 45,890', icon: 'dollar', color: '#7CB8D1', trend: { value: 12.5, isUp: true } },
    { title: 'Usuarios', value: '2,341', icon: 'users', color: '#4CAF50', trend: { value: 8.2, isUp: true } },
    { title: 'Productos', value: '856', icon: 'box', color: '#FFD93D', trend: { value: 3.1, isUp: false } },
    { title: 'Pedidos', value: '127', icon: 'shopping-bag', color: '#FF5252', trend: { value: 15.8, isUp: true } }
  ];

  // Recent transactions
  transactionsColumns: TableColumn[] = [
    { key: 'id', label: 'ID', sortable: true, width: '80px' },
    { key: 'customer', label: 'Cliente', sortable: true },
    { key: 'amount', label: 'Monto', type: 'number', sortable: true },
    { key: 'date', label: 'Fecha', type: 'date', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge' }
  ];

  transactionsData = [
    { id: '#1234', customer: 'Juan Pérez', amount: 250.50, date: '2024-03-15', status: 'completado' },
    { id: '#1235', customer: 'María García', amount: 180.00, date: '2024-03-15', status: 'pendiente' },
    { id: '#1236', customer: 'Carlos López', amount: 420.75, date: '2024-03-14', status: 'completado' },
    { id: '#1237', customer: 'Ana Martínez', amount: 95.20, date: '2024-03-14', status: 'proceso' },
    { id: '#1238', customer: 'Luis Torres', amount: 310.00, date: '2024-03-13', status: 'completado' }
  ];

  transactionsActions: TableAction[] = [
    {
      label: 'Ver',
      icon: 'view',
      color: '#2196F3',
      onClick: (row) => this.viewTransaction(row)
    }
  ];

  // Top products
  topProducts = [
    { name: 'Camisa Escolar Blanca', sales: 234, revenue: 11700 },
    { name: 'Pantalón Gris', sales: 189, revenue: 9450 },
    { name: 'Falda Azul Marino', sales: 156, revenue: 7800 },
    { name: 'Zapatos Negros', sales: 142, revenue: 8520 },
    { name: 'Buzo Deportivo', sales: 128, revenue: 7680 }
  ];

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading.set(true);
    // Simular carga de datos
    setTimeout(() => {
      this.loading.set(false);
    }, 1000);
  }

  viewTransaction(row: any): void {
    console.log('Ver transacción:', row);
  }

  refreshData(): void {
    this.loadDashboardData();
  }

  exportReport(): void {
    console.log('Exportar reporte');
    // Aquí implementar lógica de exportación
  }
}