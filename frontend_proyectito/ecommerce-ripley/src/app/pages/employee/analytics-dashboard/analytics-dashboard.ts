import {
  Component, OnInit, OnDestroy, AfterViewInit,
  ViewChild, ElementRef, signal, inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { environment } from '../../../../environments/environment';

Chart.register(...registerables);

interface EmployeeData {
  pendingOrders: number;
  processedToday: number;
  totalOrdersInPeriod: number;
  lowStockAlerts: number;
  avgProcessingRevenue: number;
  processingRate: number;
  dailyOrders: { date: string; pending: number; completed: number; total: number }[];
  ordersByStatus: { status: string; count: number; percentage: number }[];
  pendingOrdersList: { orderId: string; customer: string; total: number; status: string; date: string }[];
  lowStockProducts: { variantId: number; name: string; stock: number; category: string }[];
}

@Component({
  selector: 'app-employee-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, DashboardSidebarComponent],
  templateUrl: './analytics-dashboard.html',
  styleUrls: ['./analytics-dashboard.scss']
})
export class EmployeeAnalyticsDashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  private http = inject(HttpClient);

  @ViewChild('ordersChart') ordersChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('statusChart') statusChartRef!: ElementRef<HTMLCanvasElement>;

  loading = signal(true);
  selectedPeriod = '30d';
  data: EmployeeData | null = null;

  private ordersChartInstance: Chart | null = null;
  private statusChartInstance: Chart | null = null;

  readonly periods = [
    { value: '7d',  label: 'Últimos 7 días' },
    { value: '30d', label: 'Últimos 30 días' },
    { value: '90d', label: 'Últimos 90 días' }
  ];

  readonly statusColors: Record<string, string> = {
    'Pendiente':  '#FF9800', 'Confirmado': '#2196F3', 'En proceso': '#9C27B0',
    'Enviado':    '#00BCD4', 'Entregado':  '#4CAF50', 'Cancelado':  '#F44336',
    'Listo para retiro': '#AB47BC', 'Desconocido':'#9E9E9E'
  };

  ngOnInit(): void { this.loadData(); }
  ngAfterViewInit(): void {}
  ngOnDestroy(): void {
    this.ordersChartInstance?.destroy();
    this.statusChartInstance?.destroy();
  }

  loadData(): void {
    this.loading.set(true);
    this.http.get<any>(`${environment.apiUrl}/api/employee/dashboard/stats?period=${this.selectedPeriod}`).subscribe({
      next: (res) => {
        this.data = res.data;
        this.loading.set(false);
        setTimeout(() => this.buildCharts(), 100);
      },
      error: (err) => {
        console.error('Error al cargar dashboard empleado:', err);
        this.loading.set(false);
      }
    });
  }

  onPeriodChange(): void { this.loadData(); }

  private buildCharts(): void {
    if (!this.data) return;
    this.buildOrdersChart();
    this.buildStatusChart();
  }

  private buildOrdersChart(): void {
    if (!this.ordersChartRef?.nativeElement) return;
    this.ordersChartInstance?.destroy();
    const daily = this.data!.dailyOrders;
    const step = daily.length > 30 ? 7 : daily.length > 14 ? 3 : 1;
    const labels   = daily.filter((_, i) => i % step === 0 || i === daily.length - 1).map(d => d.date.slice(5));
    const pending   = daily.filter((_, i) => i % step === 0 || i === daily.length - 1).map(d => d.pending);
    const completed = daily.filter((_, i) => i % step === 0 || i === daily.length - 1).map(d => d.completed);

    this.ordersChartInstance = new Chart(this.ordersChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          { label: 'Pendientes', data: pending,   backgroundColor: 'rgba(245, 158, 11, 0.75)', borderColor: '#f59e0b', borderWidth: 2, borderRadius: 6 },
          { label: 'Procesados', data: completed, backgroundColor: 'rgba(16, 185, 129, 0.75)', borderColor: '#10b981', borderWidth: 2, borderRadius: 6 }
        ]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { position: 'top', labels: { color: '#757575', font: { size: 12 } } } },
        scales: {
          x: { stacked: false, grid: { display: false }, ticks: { color: '#757575', font: { size: 11 } } },
          y: { stacked: false, grid: { color: 'rgba(0,0,0,0.06)' }, ticks: { color: '#757575', font: { size: 11 } } }
        }
      }
    });
  }

  private buildStatusChart(): void {
    if (!this.statusChartRef?.nativeElement) return;
    this.statusChartInstance?.destroy();
    const byStatus = this.data!.ordersByStatus;
    if (!byStatus.length) return;

    this.statusChartInstance = new Chart(this.statusChartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: byStatus.map(s => s.status),
        datasets: [{
          data: byStatus.map(s => s.count),
          backgroundColor: byStatus.map(s => this.statusColors[s.status] || '#9E9E9E'),
          borderWidth: 3, borderColor: '#FFFFFF', hoverOffset: 8
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false, cutout: '65%',
        plugins: {
          legend: { position: 'bottom', labels: { color: '#757575', padding: 12, font: { size: 12 } } }
        }
      }
    });
  }

  formatCurrency(v: number): string {
    return `S/ ${v.toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      'Pendiente': 'status-pending', 'Confirmado': 'status-confirmed', 'En proceso': 'status-processing',
      'Enviado': 'status-shipped', 'Entregado': 'status-delivered', 'Cancelado': 'status-cancelled'
    };
    return map[status] || 'status-default';
  }

  getStockClass(stock: number): string {
    if (stock === 0) return 'stock-critical';
    if (stock <= 2)  return 'stock-warning';
    return 'stock-low';
  }
}
