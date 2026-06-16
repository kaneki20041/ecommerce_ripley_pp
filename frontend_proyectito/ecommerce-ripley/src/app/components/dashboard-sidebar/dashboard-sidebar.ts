import { Component, Input, Output, EventEmitter, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { OrderRequestService } from '../../services/order-request.service';
import { FavoriteService } from '../../services/favorite.service';

export interface MenuItem {
  label: string;
  icon: string;
  route: string;
  badge?: number;
  badgeDynamic?: boolean; // true = computed from API
  badgeDynamicType?: string; // e.g. 'favorites'
  roles: string[];
  section: 'Admin' | 'Empleado' | 'Mi Cuenta';
  children?: MenuItem[];
}

@Component({
  selector: 'app-dashboard-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard-sidebar.html',
  styleUrls: ['./dashboard-sidebar.scss']
})
export class DashboardSidebarComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);
  private orderRequestService = inject(OrderRequestService);
  private favoriteService = inject(FavoriteService);

  @Input() collapsed = false;
  @Output() collapsedChange = new EventEmitter<boolean>();

  currentUser = this.authService.currentUser;
  userRole = this.authService.userRole;

  // Dynamic badge counts
  pendingRequestsCount = signal(0);

  ngOnInit(): void {
    const role = this.userRole();
    if (role === 'ADMIN' || role === 'EMPLOYEE') {
      this.orderRequestService.getAllRequests().subscribe({
        next: (data) => {
          const pending = data.filter((r: any) =>
            (r.status || '').toUpperCase() === 'PENDIENTE'
          ).length;
          this.pendingRequestsCount.set(pending);
        },
        error: () => { /* ignore silently */ }
      });
    }
    
    if (role === 'USER' || role === 'ADMIN' || role === 'EMPLOYEE') {
      // Preload favorites count
      this.favoriteService.getFavorites().subscribe();
    }
  }

  // Menú items por rol
  menuItems = computed<MenuItem[]>(() => {
    const role = this.userRole();
    const allItems: MenuItem[] = [
      // ADMIN MENU
      {
        label: 'Dashboard',
        icon: 'analytics',
        route: '/admin/dashboard',
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Usuarios',
        icon: 'users',
        route: '/admin/usuarios',
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Gestión de Productos',
        icon: 'box',
        route: '/admin/productos',
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Inventario',
        icon: 'inventory',
        route: '/admin/inventario',
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Gestión de Pedidos',
        icon: 'shopping-bag',
        route: '/admin/pedidos',
        badge: 5,
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Cupones',
        icon: 'ticket',
        route: '/admin/cupones',
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Categorías',
        icon: 'folder',
        route: '/admin/categorias',
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Carrito Inteligente',
        icon: 'lightning',
        route: '/admin/smart-cart',
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Comunicaciones CRM',
        icon: 'envelope',
        route: '/admin/crm',
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Devoluciones',
        icon: 'return',
        route: '/admin/devoluciones',
        badgeDynamic: true,
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Reportes',
        icon: 'chart',
        route: '/admin/reportes',
        roles: ['ADMIN'],
        section: 'Admin'
      },
      {
        label: 'Configuración',
        icon: 'settings',
        route: '/admin/configuracion',
        roles: ['ADMIN'],
        section: 'Admin'
      },

      // EMPLOYEE MENU
      {
        label: 'Dashboard',
        icon: 'analytics',
        route: '/employee/dashboard',
        roles: ['EMPLOYEE'],
        section: 'Empleado'
      },
      {
        label: 'Inventario',
        icon: 'inventory',
        route: '/employee/inventario',
        roles: ['EMPLOYEE'],
        section: 'Empleado'
      },
      {
        label: 'Gestión de Productos',
        icon: 'box',
        route: '/employee/productos',
        roles: ['EMPLOYEE'],
        section: 'Empleado'
      },
      {
        label: 'Gestión de Pedidos',
        icon: 'shopping-bag',
        route: '/employee/pedidos',
        badge: 5,
        roles: ['EMPLOYEE'],
        section: 'Empleado'
      },
      {
        label: 'Devoluciones',
        icon: 'return',
        route: '/employee/devoluciones',
        badgeDynamic: true,
        roles: ['EMPLOYEE'],
        section: 'Empleado'
      },
      {
        label: 'Categorías',
        icon: 'folder',
        route: '/employee/categorias',
        roles: ['EMPLOYEE'],
        section: 'Empleado'
      },
      {
        label: 'Reportes',
        icon: 'chart',
        route: '/employee/reportes',
        roles: ['EMPLOYEE'],
        section: 'Empleado'
      },

      // USER & SHARED ACCOUNT MENU
      {
        label: 'Mi Perfil',
        icon: 'user',
        route: '/cuenta/perfil',
        roles: ['USER', 'ADMIN', 'EMPLOYEE'],
        section: 'Mi Cuenta'
      },
      {
        label: 'Mis Pedidos',
        icon: 'shopping-bag',
        route: '/cuenta/pedidos',
        roles: ['USER', 'ADMIN', 'EMPLOYEE'],
        section: 'Mi Cuenta'
      },
      {
        label: 'Favoritos',
        icon: 'heart',
        route: '/cuenta/favoritos',
        badgeDynamic: true,
        badgeDynamicType: 'favorites',
        roles: ['USER', 'ADMIN', 'EMPLOYEE'],
        section: 'Mi Cuenta'
      },
      {
        label: 'Mis Solicitudes',
        icon: 'return',
        route: '/cuenta/solicitudes',
        roles: ['USER', 'ADMIN', 'EMPLOYEE'],
        section: 'Mi Cuenta'
      },
      {
        label: 'Mis Cupones',
        icon: 'ticket',
        route: '/cuenta/cupones',
        roles: ['USER', 'ADMIN', 'EMPLOYEE'],
        section: 'Mi Cuenta'
      }
    ];

    return allItems.filter(item => item.roles.includes(role));
  });

  getBadgeValue(item: any): number {
    if (item.badgeDynamic) {
      if (item.badgeDynamicType === 'favorites') {
        return this.favoriteService.favoritesCount();
      }
      return this.pendingRequestsCount();
    }
    return item.badge ?? 0;
  }

  // Agrupar items por sección
  menuSections = computed<{ title: string; items: MenuItem[] }[]>(() => {
    const items = this.menuItems();
    const sectionsMap: { [key: string]: MenuItem[] } = {};
    
    items.forEach(item => {
      if (!sectionsMap[item.section]) {
        sectionsMap[item.section] = [];
      }
      sectionsMap[item.section].push(item);
    });
    
    const orderedSections = [
      { key: 'Admin', title: 'Administración' },
      { key: 'Empleado', title: 'Panel Empleado' },
      { key: 'Mi Cuenta', title: 'Mi Cuenta' }
    ];
    
    const result: { title: string; items: MenuItem[] }[] = [];
    orderedSections.forEach(sec => {
      if (sectionsMap[sec.key] && sectionsMap[sec.key].length > 0) {
        result.push({
          title: sec.title,
          items: sectionsMap[sec.key]
        });
      }
    });
    
    return result;
  });

  toggleSidebar(): void {
    this.collapsed = !this.collapsed;
    this.collapsedChange.emit(this.collapsed);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/home']);
  }

  getRoleLabel(): string {
    const role = this.userRole();
    const labels: { [key: string]: string } = {
      'ADMIN': 'Administrador',
      'EMPLOYEE': 'Empleado',
      'USER': 'Usuario'
    };
    return labels[role] || '';
  }

  getRoleColor(): string {
    const role = this.userRole();
    const colors: { [key: string]: string } = {
      'ADMIN': '#FF5252',
      'EMPLOYEE': '#FFD93D',
      'USER': '#4CAF50'
    };
    return colors[role] || '#666';
  }
}