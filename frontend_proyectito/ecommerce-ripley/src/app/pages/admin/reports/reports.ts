import { Component, signal, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { StatsCardComponent } from '../../../components/stats-card/stats-card';
import { ReportService, ReportStatDto } from '../../../services/report.service';
import { ToastService } from '../../../services/toast.service';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as xlsx from 'xlsx';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardSidebarComponent, StatsCardComponent],
  templateUrl: './reports.html',
  styleUrls: ['./reports.scss']
})
export class ReportsComponent implements OnInit {
  loading = signal(false);
  reportType = signal('sales');
  
  // By default, set last 30 days to today
  private today = new Date();
  private thirtyDaysAgo = new Date(new Date().setDate(new Date().getDate() - 30));
  
  dateFrom = signal(this.thirtyDaysAgo.toISOString().split('T')[0]);
  dateTo = signal(this.today.toISOString().split('T')[0]);

  stats = signal<ReportStatDto[]>([]);
  details = signal<any[]>([]);
  columns = signal<string[]>([]);

  private reportService = inject(ReportService);
  private toastService = inject(ToastService);

  ngOnInit(): void {
    this.generateReport();
  }

  generateReport(): void {
    if (!this.dateFrom() || !this.dateTo()) {
      this.toastService.error('Por favor seleccione fechas válidas');
      return;
    }

    if (new Date(this.dateFrom()) > new Date(this.dateTo())) {
      this.toastService.error('La fecha de inicio no puede ser mayor a la fecha de fin');
      return;
    }

    this.loading.set(true);
    this.details.set([]);
    this.columns.set([]);
    
    forkJoin({
      statsRes: this.reportService.getReportStats(this.reportType(), this.dateFrom(), this.dateTo()),
      detailsRes: this.reportService.getReportDetails(this.reportType(), this.dateFrom(), this.dateTo())
    }).subscribe({
      next: (result) => {
        this.stats.set(result.statsRes.data);
        const dataDetails = result.detailsRes.data || [];
        this.details.set(dataDetails);
        if (dataDetails.length > 0) {
          this.columns.set(Object.keys(dataDetails[0]));
        }
        this.toastService.success(result.statsRes.message || 'Reporte generado exitosamente');
        this.loading.set(false);
      },
      error: (err) => {
        console.error(err);
        this.toastService.error('Error al generar el reporte');
        this.loading.set(false);
      }
    });
  }

  exportPDF(): void {
    if (this.details().length === 0) {
      this.toastService.error('No hay datos detallados para exportar');
      return;
    }
    
    this.toastService.success('Generando y descargando PDF...');
    try {
      const doc = new jsPDF();
      doc.text(`Reporte Detallado de ${this.reportType()} (${this.dateFrom()} a ${this.dateTo()})`, 14, 15);
      
      const head = [this.columns()];
      const data = this.details().map(item => this.columns().map(col => item[col]));
      
      autoTable(doc, {
        head: head,
        body: data,
        startY: 20
      });
      
      doc.save(`reporte_${this.reportType()}_${this.dateFrom()}_${this.dateTo()}.pdf`);
    } catch (error) {
      console.error('Error generando PDF:', error);
      this.toastService.error('Hubo un problema al generar el PDF.');
    }
  }

  exportExcel(): void {
    if (this.details().length === 0) {
      this.toastService.error('No hay datos detallados para exportar');
      return;
    }

    this.toastService.success('Generando y descargando Excel...');
    try {
      const worksheet = xlsx.utils.json_to_sheet(this.details());
      const workbook = { Sheets: { 'data': worksheet }, SheetNames: ['data'] };
      
      xlsx.writeFile(workbook, `reporte_${this.reportType()}_${this.dateFrom()}_${this.dateTo()}.xlsx`);
    } catch (error) {
      console.error('Error generando Excel:', error);
      this.toastService.error('Hubo un problema al generar el Excel.');
    }
  }
}