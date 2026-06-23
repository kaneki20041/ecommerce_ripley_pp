import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-crm',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardSidebarComponent],
  templateUrl: './crm.html',
  styleUrl: './crm.scss'
})
export class CrmComponent implements OnInit {
  config = {
    active: true,
    daysWithoutPurchase: 30,
    discountPercentage: 10
  };

  constructor(
    private http: HttpClient,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.loadConfig();
  }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  loadConfig() {
    this.http.get<any>('https://ecommerce-ripley-pp-1.onrender.com/api/admin/crm/config', { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.config = data;
        },
        error: (err) => console.error('Error loading CRM config', err)
      });
  }

  saveConfig() {
    this.http.post<any>('https://ecommerce-ripley-pp-1.onrender.com/api/admin/crm/config', this.config, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.toastService.success('CRM', 'Configuración guardada exitosamente.');
          this.config = data;
        },
        error: (err) => {
          this.toastService.error('Error', 'Error al guardar la configuración.');
          console.error(err);
        }
      });
  }

  testCrm() {
    if(confirm('Esto forzará la ejecución del programador diario de envíos (CRM). ¿Deseas continuar?')) {
      this.http.post('https://ecommerce-ripley-pp-1.onrender.com/api/admin/crm/test-trigger', {}, { headers: this.getHeaders(), responseType: 'text' })
        .subscribe({
          next: (res) => this.toastService.success('CRM', res),
          error: (err) => this.toastService.error('Error', 'Error al ejecutar la prueba CRM')
        });
    }
  }
}
