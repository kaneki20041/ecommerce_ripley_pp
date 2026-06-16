import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { CouponService, Coupon } from '../../../services/coupon.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-user-coupons',
  standalone: true,
  imports: [CommonModule, DashboardSidebarComponent],
  templateUrl: './coupons.html',
  styleUrls: ['./coupons.scss']
})
export class UserCouponsComponent implements OnInit {
  private couponService = inject(CouponService);
  private toastService = inject(ToastService);

  coupons = signal<Coupon[]>([]);
  loading = signal(false);

  ngOnInit(): void {
    this.loadMyCoupons();
  }

  loadMyCoupons(): void {
    this.loading.set(true);
    this.couponService.getMyCoupons().subscribe({
      next: (response) => {
        if (response.result && response.data) {
          // Filtrar los que no están expirados o inactivos si lo deseamos, pero mostraremos todos
          this.coupons.set(response.data);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error fetching user coupons', error);
        this.toastService.error('Error', 'No se pudieron cargar tus cupones');
        this.loading.set(false);
      }
    });
  }

  copyCode(code: string): void {
    navigator.clipboard.writeText(code);
    this.toastService.success('¡Copiado!', `Código "${code}" copiado al portapapeles`);
  }

  getDiscountDisplay(coupon: Coupon): string {
    return coupon.type === 'percentage' 
      ? `${coupon.discount}%` 
      : `S/ ${coupon.discount}`;
  }

  isExpiringSoon(endDateStr: string): boolean {
    if (!endDateStr) return false;
    const end = new Date(endDateStr);
    const now = new Date();
    const diffTime = end.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays > 0 && diffDays <= 3; // Menos de 3 días
  }
}
