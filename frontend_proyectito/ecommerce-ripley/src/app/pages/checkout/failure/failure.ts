import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';

@Component({
  selector: 'app-checkout-failure',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './failure.html',
  styleUrls: ['./failure.scss']
})
export class CheckoutFailureComponent implements OnInit {
  private route = inject(ActivatedRoute);

  orderId = signal<string | null>(null);
  errorStatus = signal<string>('');

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.orderId.set(params['orderId'] || null);
      this.errorStatus.set(params['status'] || 'rejected');
    });
  }
}
