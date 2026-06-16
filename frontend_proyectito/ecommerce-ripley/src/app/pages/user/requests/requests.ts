import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { OrderRequestService } from '../../../services/order-request.service';

@Component({
  selector: 'app-user-requests',
  standalone: true,
  imports: [CommonModule, DashboardSidebarComponent],
  templateUrl: './requests.html',
  styleUrls: ['./requests.scss']
})
export class RequestsComponent implements OnInit {
  private orderRequestService = inject(OrderRequestService);
  
  loading = signal(true);
  requests = signal<any[]>([]);

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests(): void {
    this.orderRequestService.getUserRequests().subscribe({
      next: (data) => {
        this.requests.set(data.sort((a: any, b: any) => b.id - a.id));
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando solicitudes:', err);
        this.loading.set(false);
      }
    });
  }
}
