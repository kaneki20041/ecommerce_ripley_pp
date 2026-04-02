import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterModule, DashboardSidebarComponent],
  templateUrl: './orders.html',
  styleUrls: ['./orders.scss']
})
export class OrdersComponent {
  orders = [
    {
      id: '#1234',
      date: '2024-03-15',
      status: 'completado',
      total: 250.50,
      items: 3,
      products: ['/assets/products/1.jpg', '/assets/products/2.jpg']
    },
    {
      id: '#1235',
      date: '2024-03-10',
      status: 'enviado',
      total: 180.00,
      items: 2,
      products: ['/assets/products/3.jpg']
    }
  ];
}