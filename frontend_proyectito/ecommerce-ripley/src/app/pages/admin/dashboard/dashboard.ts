import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { StatsCardComponent } from '../../../components/stats-card/stats-card';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { RestockModalComponent, RestockItem } from '../../../components/restock-modal/restock-modal';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, DashboardSidebarComponent, StatsCardComponent, DataTableComponent, RestockModalComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class AdminDashboardComponent implements OnInit {
  private http = inject(HttpClient);
  loading = signal(false);
  loadingLowStock = signal(false);
  restocking = signal(false);
  sidebarCollapsed = false;

  stats = [
    { title: 'Productos Activos', value: '0', icon: 'box', color: '#FFD93D', trend: { value: 0, isUp: true } },
    { title: 'Pedidos Pendientes', value: '0', icon: 'shopping-bag', color: '#FF5252', trend: { value: 0, isUp: false } },
    { title: 'Stock Bajo', value: '0', icon: 'trending-up', color: '#FF9800', trend: { value: 0, isUp: true } },
  ];

  transactionsColumns: TableColumn[] = [
    { key: 'id', label: 'ID', sortable: true, width: '80px' },
    { key: 'customer', label: 'Cliente', sortable: true },
    { key: 'total', label: 'Monto', type: 'number', sortable: true },
    { key: 'date', label: 'Fecha', type: 'date', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge' }
  ];

  transactionsData: any[] = [];

  transactionsActions: TableAction[] = [
    {
      label: 'Ver',
      icon: 'view',
      color: '#2196F3',
      onClick: (row) => this.viewTransaction(row)
    }
  ];

  lowStockProducts: any[] = [];
  lowStockPage = 0;
  lowStockSize = 5;
  lowStockTotalPages = 0;
  lowStockTotalElements = 0;

  // Modal de restock
  restockModalVisible = false;
  restockItem: RestockItem | null = null;
  restockSuccessMsg = '';

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading.set(true);
    this.http.get<any>(`${environment.apiUrl}/api/admin/inventario/stats`).subscribe({
      next: (response) => {
        if (response && response.data) {
          const d = response.data;
          this.stats = [
            { title: 'Productos Activos', value: d.activeProductsCount?.toString() || '0', icon: 'box', color: '#FFD93D', trend: { value: 5.2, isUp: true } },
            { title: 'Pedidos Pendientes', value: d.pendingOrdersCount?.toString() || '0', icon: 'shopping-bag', color: '#FF5252', trend: { value: 12.0, isUp: false } },
            { title: 'Stock Bajo', value: d.lowStockProductsCount?.toString() || '0', icon: 'trending-up', color: '#FF9800', trend: { value: 2.5, isUp: true } },
          ];
          this.transactionsData = (d.pendingOrders || []).map((o: any) => ({
            id: o.id, customer: o.customer, total: o.total, date: o.date, status: o.status
          }));
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar stats de inventario:', err);
        this.loading.set(false);
      }
    });
    this.loadLowStockPage(0);
  }

  loadLowStockPage(page: number): void {
    this.loadingLowStock.set(true);
    this.http.get<any>(`${environment.apiUrl}/api/admin/inventario/low-stock?page=${page}&size=${this.lowStockSize}`).subscribe({
      next: (response) => {
        if (response && response.data) {
          const pageData = response.data;
          this.lowStockProducts = pageData.content || [];
          this.lowStockPage = pageData.number || 0;
          this.lowStockTotalPages = pageData.totalPages || 0;
          this.lowStockTotalElements = pageData.totalElements || 0;
        }
        this.loadingLowStock.set(false);
      },
      error: (err) => {
        console.error('Error al cargar low stock paginado:', err);
        this.loadingLowStock.set(false);
      }
    });
  }

  prevLowStockPage(): void {
    if (this.lowStockPage > 0) this.loadLowStockPage(this.lowStockPage - 1);
  }

  nextLowStockPage(): void {
    if (this.lowStockPage < this.lowStockTotalPages - 1) this.loadLowStockPage(this.lowStockPage + 1);
  }

  // ─── Modal de Restock ────────────────────────────────────────────────────────
  openRestockModal(product: any): void {
    this.restockItem = {
      variantId: product.variantId,
      sku: product.sku || 'N/A',
      name: product.name,
      stock: product.stock,
      category: product.category,
      price: product.price || 0
    };
    this.restockModalVisible = true;
    this.restockSuccessMsg = '';
  }

  onRestockClosed(): void {
    this.restockModalVisible = false;
    this.restockItem = null;
  }

  onRestockConfirmed(event: { variantId: number; cantidad: number }): void {
    this.restocking.set(true);
    this.http.put<any>(
      `${environment.apiUrl}/api/admin/inventario/restock/${event.variantId}?cantidad=${event.cantidad}`, {}
    ).subscribe({
      next: (res) => {
        this.restocking.set(false);
        this.restockModalVisible = false;
        this.restockSuccessMsg = `✅ Stock repuesto correctamente. Nuevo stock: ${res.data} uds.`;
        this.loadLowStockPage(this.lowStockPage);
        this.loadDashboardData();
        setTimeout(() => this.restockSuccessMsg = '', 4000);
      },
      error: (err) => {
        this.restocking.set(false);
        console.error('Error al reponer stock:', err);
      }
    });
  }

  viewTransaction(row: any): void {
    console.log('Ver transacción:', row);
  }

  refreshData(): void {
    this.loadDashboardData();
  }

  exportReport(): void {
    console.log('Exportar reporte');
  }
}