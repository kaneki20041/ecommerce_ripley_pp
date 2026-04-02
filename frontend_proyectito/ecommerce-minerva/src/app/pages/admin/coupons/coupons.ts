import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { ModalComponent } from '../../../components/modal/modal';

interface Coupon {
  id: number;
  code: string;
  discount: number;
  type: 'percentage' | 'fixed';
  startDate: string;
  endDate: string;
  usageLimit: number;
  usageCount: number;
  status: 'activo' | 'inactivo' | 'expirado';
  minPurchase: number;
}

@Component({
  selector: 'app-coupons',
  standalone: true,
  imports: [FormsModule, DashboardSidebarComponent, DataTableComponent, ModalComponent],
  templateUrl: './coupons.html',
  styleUrls: ['./coupons.scss']
})
export class CouponsComponent implements OnInit {
  loading = signal(false);
  showCreateModal = signal(false);
  showEditModal = signal(false);
  
  // Form data
  couponForm = {
    id: 0,
    code: '',
    discount: 0,
    type: 'percentage' as 'percentage' | 'fixed',
    startDate: '',
    endDate: '',
    usageLimit: 100,
    minPurchase: 0
  };

  // Filters
  statusFilter = signal<string>('all');

  // Table config
  columns: TableColumn[] = [
    { key: 'code', label: 'Código', sortable: true },
    { key: 'discount', label: 'Descuento', sortable: true },
    { key: 'usageCount', label: 'Usos', type: 'number', sortable: true },
    { key: 'usageLimit', label: 'Límite', type: 'number', sortable: true },
    { key: 'endDate', label: 'Expira', type: 'date', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge', sortable: true }
  ];

  couponsData = signal<Coupon[]>([
    {
      id: 1,
      code: 'DESCUENTO10',
      discount: 10,
      type: 'percentage',
      startDate: '2024-01-01',
      endDate: '2024-12-31',
      usageLimit: 100,
      usageCount: 45,
      status: 'activo',
      minPurchase: 50
    },
    {
      id: 2,
      code: 'PRIMERACOMPRA',
      discount: 15,
      type: 'percentage',
      startDate: '2024-01-01',
      endDate: '2024-06-30',
      usageLimit: 500,
      usageCount: 234,
      status: 'activo',
      minPurchase: 0
    },
    {
      id: 3,
      code: 'VERANO2024',
      discount: 20,
      type: 'percentage',
      startDate: '2024-01-01',
      endDate: '2024-03-01',
      usageLimit: 200,
      usageCount: 200,
      status: 'expirado',
      minPurchase: 100
    },
    {
      id: 4,
      code: 'FIJO50',
      discount: 50,
      type: 'fixed',
      startDate: '2024-03-01',
      endDate: '2024-12-31',
      usageLimit: 50,
      usageCount: 12,
      status: 'activo',
      minPurchase: 200
    }
  ]);

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
    setTimeout(() => {
      this.loading.set(false);
    }, 800);
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
    const newCoupon: Coupon = {
      id: this.couponsData().length + 1,
      code: this.couponForm.code.toUpperCase(),
      discount: this.couponForm.discount,
      type: this.couponForm.type,
      startDate: this.couponForm.startDate,
      endDate: this.couponForm.endDate,
      usageLimit: this.couponForm.usageLimit,
      usageCount: 0,
      status: 'activo',
      minPurchase: this.couponForm.minPurchase
    };

    this.couponsData.set([newCoupon, ...this.couponsData()]);
    this.closeCreateModal();
  }

  editCoupon(coupon: any): void {
    this.couponForm = {
      id: coupon.id,
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
    const coupons = this.couponsData();
    const index = coupons.findIndex(c => c.id === this.couponForm.id);
    
    if (index !== -1) {
      coupons[index] = {
        ...coupons[index],
        code: this.couponForm.code.toUpperCase(),
        discount: this.couponForm.discount,
        type: this.couponForm.type,
        startDate: this.couponForm.startDate,
        endDate: this.couponForm.endDate,
        usageLimit: this.couponForm.usageLimit,
        minPurchase: this.couponForm.minPurchase
      };
      this.couponsData.set([...coupons]);
    }
    
    this.closeEditModal();
  }

  copyCoupon(coupon: any): void {
    navigator.clipboard.writeText(coupon.code);
    alert(`Código "${coupon.code}" copiado al portapapeles`);
  }

  toggleStatus(coupon: any): void {
    const coupons = this.couponsData();
    const index = coupons.findIndex(c => c.id === coupon.id);
    
    if (index !== -1) {
      coupons[index].status = coupon.status === 'activo' ? 'inactivo' : 'activo';
      this.couponsData.set([...coupons]);
    }
  }

  deleteCoupon(coupon: any): void {
    if (confirm(`¿Estás seguro de eliminar el cupón "${coupon.code}"?`)) {
      this.couponsData.set(this.couponsData().filter(c => c.id !== coupon.id));
    }
  }

  resetForm(): void {
    this.couponForm = {
      id: 0,
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
}