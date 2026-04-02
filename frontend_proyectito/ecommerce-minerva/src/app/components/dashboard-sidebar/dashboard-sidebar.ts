import { Component, Input, Output, EventEmitter, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';

export interface MenuItem {
  label: string;
  icon: string;
  route: string;
  badge?: number;
  roles: string[];
  children?: MenuItem[];
}

@Component({
  selector: 'app-dashboard-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard-sidebar.html',
  styleUrls: ['./dashboard-sidebar.scss']
})
export class DashboardSidebarComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  @Input() collapsed = false;
  @Output() collapsedChange = new EventEmitter<boolean>();

  currentUser = this.authService.currentUser;
  userRole = this.authService.userRole;

  // Menú items por rol
  menuItems = computed<MenuItem[]>(() => {
    const role = this.userRole();
    const allItems: MenuItem[] = [
      // ADMIN MENU
      {
        label: 'Dashboard',
        icon: 'dashboard',
        route: '/admin/dashboard',
        roles: ['ADMIN']
      },
      {
        label: 'Usuarios',
        icon: 'users',
        route: '/admin/usuarios',
        roles: ['ADMIN']
      },
      {
        label: 'Cupones',
        icon: 'ticket',
        route: '/admin/cupones',
        roles: ['ADMIN']
      },
      {
        label: 'Devoluciones',
        icon: 'return',
        route: '/admin/devoluciones',
        badge: 3,
        roles: ['ADMIN']
      },
      {
        label: 'Reportes',
        icon: 'chart',
        route: '/admin/reportes',
        roles: ['ADMIN']
      },
      {
        label: 'Configuración',
        icon: 'settings',
        route: '/admin/configuracion',
        roles: ['ADMIN']
      },

      // EMPLOYEE MENU
      {
        label: 'Dashboard',
        icon: 'dashboard',
        route: '/employee/dashboard',
        roles: ['EMPLOYEE']
      },
      {
        label: 'Productos',
        icon: 'box',
        route: '/employee/productos',
        roles: ['EMPLOYEE']
      },
      {
        label: 'Pedidos',
        icon: 'shopping-bag',
        route: '/employee/pedidos',
        badge: 5,
        roles: ['EMPLOYEE']
      },
      {
        label: 'Devoluciones',
        icon: 'return',
        route: '/employee/devoluciones',
        roles: ['EMPLOYEE']
      },
      {
        label: 'Mi Perfil',
        icon: 'user',
        route: '/employee/perfil',
        roles: ['EMPLOYEE']
      },

      // USER MENU
      {
        label: 'Mi Perfil',
        icon: 'user',
        route: '/cuenta/perfil',
        roles: ['USER']
      },
      {
        label: 'Mis Pedidos',
        icon: 'shopping-bag',
        route: '/cuenta/pedidos',
        roles: ['USER']
      },
      {
        label: 'Favoritos',
        icon: 'heart',
        route: '/cuenta/favoritos',
        badge: 12,
        roles: ['USER']
      }
    ];

    return allItems.filter(item => item.roles.includes(role));
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