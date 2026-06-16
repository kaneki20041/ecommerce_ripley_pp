import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { StatsCardComponent } from '../../../components/stats-card/stats-card';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { RestockModalComponent, RestockItem } from '../../../components/restock-modal/restock-modal';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, DashboardSidebarComponent, StatsCardComponent, DataTableComponent, RestockModalComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  loading = signal(false);
  loadingLowStock = signal(false);
  restocking = signal(false);

  // Stats data
  stats = [
    { title: 'Productos Activos', value: '0', icon: 'box', color: '#FFD93D', trend: { value: 0, isUp: true } },
    { title: 'Pedidos Pendientes', value: '0', icon: 'shopping-bag', color: '#FF5252', trend: { value: 0, isUp: false } },
    { title: 'Stock Bajo', value: '0', icon: 'trending-up', color: '#FF9800', trend: { value: 0, isUp: true } }
  ];

  pendingOrdersColumns: TableColumn[] = [
    { key: 'id', label: 'ID', width: '100px' },
    { key: 'customer', label: 'Cliente', sortable: true },
    { key: 'total', label: 'Total', type: 'number', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge' },
    { key: 'date', label: 'Fecha', type: 'date', sortable: true }
  ];

  pendingOrdersData: any[] = [];

  ordersActions: TableAction[] = [
    {
      label: 'Procesar',
      icon: 'check',
      color: '#4CAF50',
      onClick: (row) => this.processOrder(row),
      show: (row) => row.status === 'pendiente' || row.status === 'placed' || row.status === 'pending'
    },
    {
      label: 'Ver',
      icon: 'view',
      color: '#2196F3',
      onClick: (row) => this.viewOrder(row)
    }
  ];

  // Paginación de stock bajo
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
            { title: 'Stock Bajo', value: d.lowStockProductsCount?.toString() || '0', icon: 'trending-up', color: '#FF9800', trend: { value: 2.5, isUp: true } }
          ];
          this.pendingOrdersData = d.pendingOrders || [];
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar estadísticas del inventario:', err);
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
        console.error('Error al cargar productos con stock bajo:', err);
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

  // ─── Modal de Restock ───────────────────────────────────────────────────────
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
        // Recargar la página actual de stock bajo para reflejar cambios
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

  // ─── Otros métodos ──────────────────────────────────────────────────────────
  processOrder(row: any): void {
    const numericId = Number(row.id.replace('#', ''));
    this.http.put(`${environment.apiUrl}/api/admin/orders/${numericId}/confirmed`, {}).subscribe({
      next: () => this.loadDashboardData(),
      error: (err) => console.error('Error al procesar el pedido:', err)
    });
  }

  viewOrder(row: any): void {
    const numericId = row.id.replace('#', '');
    this.router.navigate(['/employee/pedidos'], { queryParams: { id: numericId } });
  }

  refreshData(): void {
    this.loadDashboardData();
  }
}