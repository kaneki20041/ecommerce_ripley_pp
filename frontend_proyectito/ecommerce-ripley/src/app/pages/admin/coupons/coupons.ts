import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { ModalComponent } from '../../../components/modal/modal';
import { ToastService } from '../../../services/toast.service';
import { CouponService } from '../../../services/coupon.service';

interface Coupon {
  id?: number;
  name?: string;
  code: string;
  discount: number;
  type: string; // 'percentage' | 'fixed'
  minPurchase: number;
  startDate: string;
  endDate: string;
  status?: string; // computed or from backend
  usageCount: number;
  usageLimit: number;
}

@Component({
  selector: 'app-coupons',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardSidebarComponent, DataTableComponent, ModalComponent],
  templateUrl: './coupons.html',
  styleUrls: ['./coupons.scss']
})
export class CouponsComponent implements OnInit {
  couponService = inject(CouponService);
  toastService = inject(ToastService);

  loading = signal(false);
  showCreateModal = signal(false);
  showEditModal = signal(false);

  // Form data
  couponForm = {
    id: 0,
    name: '',
    code: '',
    discount: 0,
    type: 'percentage',
    startDate: '',
    endDate: '',
    usageLimit: 100,
    minPurchase: 0
  };

  // Filters
  statusFilter = signal<string>('all');

  // Table config
  columns: TableColumn[] = [
    { key: 'name', label: 'Nombre', sortable: true },
    { key: 'code', label: 'Código', sortable: true },
    { key: 'discount', label: 'Descuento', sortable: true },
    { key: 'usageCount', label: 'Usos', type: 'number', sortable: true },
    { key: 'usageLimit', label: 'Límite', type: 'number', sortable: true },
    { key: 'endDate', label: 'Expira', type: 'date', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge', sortable: true }
  ];

  couponsData = signal<Coupon[]>([]);

  actions: TableAction[] = [
    {
      label: 'Copiar',
      icon: 'copy',
      color: '#2196F3',
      onClick: (row) => this.copyCoupon(row)
    },
    {
      label: 'Editar',
      icon: 'edit',
      color: '#4CAF50',
      onClick: (row) => this.editCoupon(row)
    },
    {
      label: 'Desactivar',
      icon: 'x',
      color: '#FF9800',
      onClick: (row) => this.toggleStatus(row),
      show: (row) => row.status === 'activo'
    },
    {
      label: 'Activar',
      icon: 'check',
      color: '#4CAF50',
      onClick: (row) => this.toggleStatus(row),
      show: (row) => row.status === 'inactivo'
    },
    {
      label: 'Eliminar',
      icon: 'delete',
      color: '#FF5252',
      onClick: (row) => this.deleteCoupon(row)
    }
  ];

  ngOnInit(): void {
    this.loadCoupons();
  }

  loadCoupons(): void {
    this.loading.set(true);
    this.couponService.getAllAdminCoupons().subscribe({
      next: (response) => {
        if (response.result && response.data) {
          this.couponsData.set(response.data);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error fetching coupons', error);
        this.showToast('Error al cargar los cupones', 'error');
        this.loading.set(false);
      }
    });
  }

  openCreateModal(): void {
    this.resetForm();
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
    this.resetForm();
  }

  createCoupon(): void {
    const newCoupon: any = {
      name: this.couponForm.name,
      code: this.couponForm.code.toUpperCase(),
      discount: this.couponForm.discount,
      type: this.couponForm.type,
      startDate: this.couponForm.startDate,
      endDate: this.couponForm.endDate,
      usageLimit: this.couponForm.usageLimit,
      minPurchase: this.couponForm.minPurchase,
      status: 'activo'
    };

    this.couponService.createCoupon(newCoupon).subscribe({
      next: (response) => {
        if (response.result && response.data) {
          this.loadCoupons();
          this.closeCreateModal();
          this.showToast('Cupón creado exitosamente', 'success');
        }
      },
      error: (error) => {
        console.error('Error creating coupon', error);
        this.showToast('Error al crear el cupón', 'error');
      }
    });
  }

  editCoupon(coupon: any): void {
    this.couponForm = {
      id: coupon.id,
      name: coupon.name || '',
      code: coupon.code,
      discount: coupon.discount,
      type: coupon.type,
      startDate: coupon.startDate,
      endDate: coupon.endDate,
      usageLimit: coupon.usageLimit,
      minPurchase: coupon.minPurchase
    };
    this.showEditModal.set(true);
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
    this.resetForm();
  }

  updateCoupon(): void {
    const couponPayload: any = {
      name: this.couponForm.name,
      code: this.couponForm.code.toUpperCase(),
      discount: this.couponForm.discount,
      type: this.couponForm.type,
      startDate: this.couponForm.startDate,
      endDate: this.couponForm.endDate,
      usageLimit: this.couponForm.usageLimit,
      minPurchase: this.couponForm.minPurchase
    };
    
    this.couponService.updateCoupon(this.couponForm.id, couponPayload).subscribe({
      next: (response) => {
        if (response.result && response.data) {
          this.loadCoupons();
          this.closeEditModal();
          this.showToast('Cupón actualizado correctamente', 'success');
        }
      },
      error: (error) => {
        console.error('Error updating coupon', error);
        this.showToast('Error al actualizar el cupón', 'error');
      }
    });
  }

  copyCoupon(coupon: any): void {
    navigator.clipboard.writeText(coupon.code);
    this.showToast(`Código "${coupon.code}" copiado al portapapeles`, 'success');
  }

  toggleStatus(coupon: any): void {
    this.couponService.toggleCouponStatus(coupon.id).subscribe({
      next: (response) => {
        if (response.result) {
          this.loadCoupons();
          this.showToast('Estado modificado', 'success');
        }
      },
      error: (error) => {
        console.error('Error toggling coupon status', error);
        this.showToast('Error al modificar estado', 'error');
      }
    });
  }

  deleteCoupon(coupon: any): void {
    if (confirm(`¿Estás seguro de eliminar el cupón "${coupon.code}"?`)) {
      this.couponService.deleteCoupon(coupon.id).subscribe({
        next: (response) => {
          if (response.result) {
            this.loadCoupons();
            this.showToast('Cupón eliminado', 'success');
          }
        },
        error: (error) => {
          console.error('Error deleting coupon', error);
          this.showToast('Error al eliminar cupón', 'error');
        }
      });
    }
  }

  resetForm(): void {
    this.couponForm = {
      id: 0,
      name: '',
      code: '',
      discount: 0,
      type: 'percentage',
      startDate: '',
      endDate: '',
      usageLimit: 100,
      minPurchase: 0
    };
  }

  getFilteredCoupons(): any[] {
    const filter = this.statusFilter();
    if (filter === 'all') return this.couponsData();
    return this.couponsData().filter(c => c.status === filter);
  }

  generateRandomCode(): void {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let code = '';
    for (let i = 0; i < 8; i++) {
      code += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    this.couponForm.code = code;
  }

  getDiscountDisplay(coupon: Coupon): string {
    return coupon.type === 'percentage' 
      ? `${coupon.discount}%` 
      : `S/ ${coupon.discount}`;
  }

  showToast(message: string, type: 'success' | 'error' = 'success'): void {
    if (type === 'success') {
      this.toastService.success('Gestión de Cupones', message);
    } else {
      this.toastService.error('Error', message);
    }
  }
}