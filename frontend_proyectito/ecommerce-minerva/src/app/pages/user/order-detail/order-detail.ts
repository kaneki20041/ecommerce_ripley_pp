import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { UpperCasePipe, DatePipe, DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [DashboardSidebarComponent, UpperCasePipe, DatePipe, DecimalPipe],
  templateUrl: './order-detail.html',
  styleUrls: ['./order-detail.scss']
})
export class OrderDetailComponent implements OnInit {
  loading = signal(false);
  order = signal<any>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    this.loadOrder(id);
  }

  loadOrder(id: string): void {
    this.loading.set(true);
    // Mock data
    setTimeout(() => {
      this.order.set({
        id: id,
        date: '2024-03-15',
        status: 'completado',
        total: 250.50,
        products: [
          { name: 'Camisa Escolar Blanca M', quantity: 2, price: 50 },
          { name: 'Pantalón Gris L', quantity: 1, price: 65 }
        ],
        shippingAddress: 'Av. Larco 1234, Miraflores',
        paymentMethod: 'Tarjeta',
        trackingNumber: 'TRK123456789'
      });
      this.loading.set(false);
    }, 800);
  }

  requestReturn(): void {
    this.router.navigate(['/cuenta/solicitar-devolucion', this.order()?.id]);
  }

  buyAgain(): void {
    console.log('Comprar de nuevo');
  }

  downloadInvoice(): void {
    console.log('Descargar factura');
  }
}