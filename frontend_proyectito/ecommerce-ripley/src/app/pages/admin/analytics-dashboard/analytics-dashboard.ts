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

interface DashboardData {
  totalRevenue: number;
  totalOrders: number;
  totalUsers: number;
  totalProducts: number;
  avgOrderValue: number;
  conversionRate: number;
  revenueGrowth: number;
  ordersGrowth: number;
  usersGrowth: number;
  dailySales: { date: string; revenue: number; orders: number }[];
  topCategories: { category: string; revenue: number; orders: number; percentage: number }[];
  ordersByStatus: { status: string; count: number; percentage: number }[];
  recentOrders: { orderId: string; customer: string; total: number; status: string; date: string; paymentMethod: string }[];
  recommendationCtr?: number;
}

@Component({
  selector: 'app-admin-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, DashboardSidebarComponent],
  templateUrl: './analytics-dashboard.html',
  styleUrls: ['./analytics-dashboard.scss']
})
export class AdminAnalyticsDashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  private http = inject(HttpClient);

  @ViewChild('salesChart')  salesChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('statusChart') statusChartRef!: ElementRef<HTMLCanvasElement>;

  loading = signal(true);
  selectedPeriod = '30d';
  data: DashboardData | null = null;

  private salesChartInstance: Chart | null = null;
  private statusChartInstance: Chart | null = null;

  readonly periods = [
    { value: '7d',   label: 'Últimos 7 días' },
    { value: '30d',  label: 'Últimos 30 días' },
    { value: '90d',  label: 'Últimos 90 días' },
    { value: '365d', label: 'Último año' }
  ];

  readonly statusColors: Record<string, string> = {
    'Pendiente':  '#FF9800', 'Confirmado': '#2196F3', 'En proceso': '#9C27B0',
    'Enviado':    '#00BCD4', 'Entregado':  '#4CAF50', 'Cancelado':  '#F44336',
    'Listo para retiro': '#AB47BC', 'Desconocido':'#9E9E9E'
  };

  ngOnInit(): void { this.loadData(); }

  ngAfterViewInit(): void {
    // Las gráficas se crean cuando llegan los datos
  }

  ngOnDestroy(): void {
    this.salesChartInstance?.destroy();
    this.statusChartInstance?.destroy();
  }

  loadData(): void {
    this.loading.set(true);
    this.http.get<any>(`${environment.apiUrl}/api/admin/dashboard/stats?period=${this.selectedPeriod}`).subscribe({
      next: (res) => {
        this.data = res.data;
        this.loading.set(false);
        setTimeout(() => this.buildCharts(), 100);
      },
      error: (err) => {
        console.error('Error al cargar dashboard:', err);
        this.loading.set(false);
      }
    });
  }

  onPeriodChange(): void { this.loadData(); }

  private buildCharts(): void {
    if (!this.data) return;
    this.buildSalesChart();
    this.buildStatusChart();
  }

  private buildSalesChart(): void {
    if (!this.salesChartRef?.nativeElement) return;
    this.salesChartInstance?.destroy();
    const daily = this.data!.dailySales;
    // Mostrar solo cada N días para no saturar el eje X
    const step = daily.length > 30 ? 7 : daily.length > 14 ? 3 : 1;
    const labels = daily.filter((_, i) => i % step === 0 || i === daily.length - 1)
      .map(d => d.date.slice(5)); // "05-01"
    const revenues = daily.filter((_, i) => i % step === 0 || i === daily.length - 1)
      .map(d => d.revenue);

    this.salesChartInstance = new Chart(this.salesChartRef.nativeElement, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: 'Ingresos (S/)',
          data: revenues,
          backgroundColor: 'rgba(124, 184, 209, 0.75)',
          borderColor: '#7CB8D1',
          borderWidth: 2,
          borderRadius: 7,
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { callbacks: {
          label: ctx => `S/ ${((ctx.parsed.y ?? 0)).toLocaleString('es-PE', { minimumFractionDigits: 2 })}`
        }}},
        scales: {
          x: { grid: { display: false }, ticks: { color: '#757575', font: { size: 11 } } },
          y: { grid: { color: 'rgba(0,0,0,0.06)' }, ticks: {
            color: '#757575', font: { size: 11 },
            callback: v => `S/ ${Number(v).toLocaleString('es-PE')}`
          }}
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
          borderWidth: 3, borderColor: '#FFFFFF',
          hoverOffset: 8
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false, cutout: '65%',
        plugins: {
          legend: { position: 'bottom', labels: { color: '#757575', padding: 12, font: { size: 12 } } },
          tooltip: { callbacks: { label: ctx => `${ctx.label}: ${ctx.parsed} pedidos` } }
        }
      }
    });
  }

  formatCurrency(v: number): string {
    return `S/ ${v.toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  formatPercent(v: number): string {
    return `${v >= 0 ? '+' : ''}${v.toFixed(1)}%`;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      'Pendiente': 'status-pending', 'Confirmado': 'status-confirmed', 'En proceso': 'status-processing',
      'Enviado': 'status-shipped', 'Entregado': 'status-delivered', 'Cancelado': 'status-cancelled'
    };
    return map[status] || 'status-default';
  }
}
