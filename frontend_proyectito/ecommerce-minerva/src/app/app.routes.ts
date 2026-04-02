import { Routes } from '@angular/router';
import { adminGuardGuard } from './guards/admin.guard-guard';
import { employeeGuardGuard } from './guards/employee.guard-guard';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
  // Ruta principal - Redirige a home
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },

  // Página principal (Home)
  {
    path: 'home',
    loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent)
  },

  // Página Nosotros
  {
    path: 'nosotros',
    loadComponent: () => import('./pages/about/about').then(m => m.AboutComponent)
  },

  // // Rutas de productos (para futuro desarrollo)
  // { 
  //   path: 'mujer/uniformes/:item', 
  //   loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent),
  //   data: { category: 'mujer', subcategory: 'uniformes' }
  // },
  // { 
  //   path: 'mujer/buzos/:item', 
  //   loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent),
  //   data: { category: 'mujer', subcategory: 'buzos' }
  // },
  // { 
  //   path: 'mujer/calzado/:item', 
  //   loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent),
  //   data: { category: 'mujer', subcategory: 'calzado' }
  // },
  // { 
  //   path: 'mujer/casual/:item', 
  //   loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent),
  //   data: { category: 'mujer', subcategory: 'casual' }
  // },
  // { 
  //   path: 'mujer/accesorios/:item', 
  //   loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent),
  //   data: { category: 'mujer', subcategory: 'accesorios' }
  // },
  // { 
  //   path: 'hombre/uniformes/:item', 
  //   loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent),
  //   data: { category: 'hombre', subcategory: 'uniformes' }
  // },
  // { 
  //   path: 'hombre/buzos/:item', 
  //   loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent),
  //   data: { category: 'hombre', subcategory: 'buzos' }
  // },
  // { 
  //   path: 'hombre/calzado/:item', 
  //   loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent),
  //   data: { category: 'hombre', subcategory: 'calzado' }
  // },
  // { 
  //   path: 'hombre/casual/:item', 
  //   loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent),
  //   data: { category: 'hombre', subcategory: 'casual' }
  // },
  // { 
  //   path: 'hombre/accesorios/:item', 
  //   loadComponent: () => import('./pages/products/products.component').then(m => m.ProductsComponent),
  //   data: { category: 'hombre', subcategory: 'accesorios' }
  // },

  // Otras páginas del footer (placeholder - crear después)
  {
    path: 'productos',
    loadComponent: () => import('./pages/products/products').then(m => m.ProductsComponent)
  },
  {
    path: 'contacto',
    loadComponent: () => import('./pages/contact/contact').then(m => m.ContactComponent)
  },
  // Productos
  {
    path: 'productos',
    loadComponent: () => import('./pages/products/products').then(m => m.ProductsComponent)
  },
  {
    path: 'producto/:id',
    loadComponent: () => import('./pages/product-detail/product-detail').then(m => m.ProductDetailComponent)
  },

  // Carrito
  {
    path: 'carrito',
    loadComponent: () => import('./pages/cart/cart').then(m => m.CartComponent)
  },
  // ============================================
  // ÁREA DE USUARIO (ROLE: USER)
  // ============================================
  {
    path: 'cuenta',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'perfil',
        pathMatch: 'full'
      },
      {
        path: 'perfil',
        loadComponent: () => import('./pages/user/profile/profile').then(m => m.ProfileComponent)
      },
      {
        path: 'pedidos',
        loadComponent: () => import('./pages/user/orders/orders').then(m => m.OrdersComponent)
      },
      {
        path: 'pedido/:id',
        loadComponent: () => import('./pages/user/order-detail/order-detail').then(m => m.OrderDetailComponent)
      },
      {
        path: 'favoritos',
        loadComponent: () => import('./pages/user/favorites/favorites').then(m => m.FavoritesComponent)
      }
    ]
  },

  // ============================================
  // ÁREA DE EMPLEADO (ROLE: EMPLOYEE)
  // ============================================
  {
    path: 'employee',
    canActivate: [employeeGuardGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/employee/dashboard/dashboard').then(m => m.DashboardComponent)
      },
      {
        path: 'productos',
        loadComponent: () => import('./pages/employee/products-management/products-management').then(m => m.ProductsManagementComponent)
      },
      {
        path: 'pedidos',
        loadComponent: () => import('./pages/employee/orders-management/orders-management').then(m => m.OrdersManagementComponent)
      },
      {
        path: 'devoluciones',
        loadComponent: () => import('./pages/employee/returns/returns').then(m => m.EmployeeReturnsComponent)
      },
      {
        path: 'perfil',
        loadComponent: () => import('./pages/employee/profile/profile').then(m => m.EmployeeProfileComponent)
      }
    ]
  },

  // ============================================
  // ÁREA DE ADMINISTRADOR (ROLE: ADMIN)
  // ============================================
  {
    path: 'admin',
    canActivate: [adminGuardGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/admin/dashboard/dashboard').then(m => m.AdminDashboardComponent)
      },
      {
        path: 'productos',
        loadComponent: () => import('./pages/employee/products-management/products-management').then(m => m.ProductsManagementComponent)
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./pages/admin/users/users').then(m => m.UsersComponent)
      },
      {
        path: 'cupones',
        loadComponent: () => import('./pages/admin/coupons/coupons').then(m => m.CouponsComponent)
      },
      {
        path: 'devoluciones',
        loadComponent: () => import('./pages/admin/returns/returns').then(m => m.AdminReturnsComponent)
      },
      {
        path: 'reportes',
        loadComponent: () => import('./pages/admin/reports/reports').then(m => m.ReportsComponent)
      },
      {
        path: 'configuracion',
        loadComponent: () => import('./pages/admin/settings/settings').then(m => m.SettingsComponent)
      }
    ]
  },
  // 404 - Not Found
  {
    path: '**',
    redirectTo: '/home'
  }
];