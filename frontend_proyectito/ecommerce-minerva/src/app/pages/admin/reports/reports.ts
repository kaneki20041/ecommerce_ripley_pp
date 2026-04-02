import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { StatsCardComponent } from '../../../components/stats-card/stats-card';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [FormsModule, DashboardSidebarComponent, StatsCardComponent],
  templateUrl: './reports.html',
  styleUrls: ['./reports.scss']
})
export class ReportsComponent {
  loading = signal(false);
  reportType = signal('sales');
  dateFrom = signal('2024-01-01');
  dateTo = signal('2024-03-20');

  stats = [
    { title: 'Ventas Totales', value: 'S/ 145,890', icon: 'dollar', color: '#7CB8D1' },
    { title: 'Pedidos', value: '1,234', icon: 'shopping-bag', color: '#4CAF50' },
    { title: 'Productos Vendidos', value: '5,678', icon: 'box', color: '#FFD93D' }
  ];

  generateReport(): void {
    this.loading.set(true);
    console.log('Generar reporte:', {
      type: this.reportType(),
      from: this.dateFrom(),
      to: this.dateTo()
    });
    setTimeout(() => this.loading.set(false), 1500);
  }

  exportPDF(): void {
    console.log('Exportar PDF');
  }

  exportExcel(): void {
    console.log('Exportar Excel');
  }
}