import { Component, HostListener, computed, inject } from '@angular/core';

import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';
import { AuthModalService } from '../../services/auth-modal';
import { CartService } from '../../services/cart.service';

interface SubMenuItem {
  label: string;
  route: string;
}

interface MenuCategory {
  header: string;
  items: SubMenuItem[];
}

interface MenuItem {
  label: string;
  categories: MenuCategory[];
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.scss'],
})
export class NavbarComponent {
  private cartService = inject(CartService);
  cartItemCount = this.cartService.cartCount;
  activeMenu: string | null = null;
  isScrolled = false;
  showUserMenu = false;

  // Computed values para autenticación
  private router = inject (Router);
  isAuthenticated = computed(() => this.authService.isAuthenticated());
  currentUser = computed(() => this.authService.currentUser());
  userInitials = computed(() => {
    const user = this.currentUser();
    if (!user || !user.firstName) return '';

    const primeraLetraNombre = user.firstName.charAt(0);
    const segundaLetra = user.lastName ? user.lastName.charAt(0) : '';
    return `${primeraLetraNombre}${segundaLetra}`.toUpperCase();
  });

  constructor(
    public authService: AuthService,
    public authModalService: AuthModalService,
  ) { }

  menuItems: MenuItem[] = [
    {
      label: 'Mujer',
      categories: [
        {
          header: 'Uniformes Escolares',
          items: [
            { label: 'Camisa', route: '/mujer/uniformes/camisa' },
            { label: 'Pantalón', route: '/mujer/uniformes/pantalon' },
            { label: 'Pullover', route: '/mujer/uniformes/pullover' },
          ],
        },
        {
          header: 'Buzos Escolares',
          items: [
            { label: 'Polo Deportivo', route: '/mujer/buzos/polo' },
            { label: 'Buzo Deportivo', route: '/mujer/buzos/buzo' },
            { label: 'Casaca Deportiva', route: '/mujer/buzos/casaca' },
          ],
        },
        {
          header: 'Calzado',
          items: [
            { label: 'Zapatillas', route: '/mujer/calzado/zapatillas' },
            { label: 'Sandalias', route: '/mujer/calzado/sandalias' },
          ],
        },
        {
          header: 'Casual',
          items: [
            { label: 'Polos Cuello Cerrado', route: '/mujer/casual/polos-cerrado' },
            { label: 'Polos Tipo Camisa', route: '/mujer/casual/polos-camisa' },
          ],
        },
        {
          header: 'Accesorios',
          items: [
            { label: 'Medias', route: '/mujer/accesorios/medias' },
            { label: 'Aretes', route: '/mujer/accesorios/aretes' },
            { label: 'Pulseras', route: '/mujer/accesorios/pulseras' },
            { label: 'Corbatas', route: '/mujer/accesorios/corbatas' },
            { label: 'Cinturones', route: '/mujer/accesorios/cinturones' },
            { label: 'Lazos', route: '/mujer/accesorios/lazos' },
          ],
        },
      ],
    },
    {
      label: 'Hombre',
      categories: [
        {
          header: 'Uniformes Escolares',
          items: [
            { label: 'Camisa', route: '/hombre/uniformes/camisa' },
            { label: 'Pantalón', route: '/hombre/uniformes/pantalon' },
            { label: 'Pullover', route: '/hombre/uniformes/pullover' },
          ],
        },
        {
          header: 'Buzos Escolares',
          items: [
            { label: 'Polo Deportivo', route: '/hombre/buzos/polo' },
            { label: 'Buzo Deportivo', route: '/hombre/buzos/buzo' },
            { label: 'Casaca Deportiva', route: '/hombre/buzos/casaca' },
          ],
        },
        {
          header: 'Calzado',
          items: [
            { label: 'Zapatillas', route: '/hombre/calzado/zapatillas' },
            { label: 'Sandalias', route: '/hombre/calzado/sandalias' },
          ],
        },
        {
          header: 'Casual',
          items: [
            { label: 'Polos Cuello Cerrado', route: '/hombre/casual/polos-cerrado' },
            { label: 'Polos Tipo Camisa', route: '/hombre/casual/polos-camisa' },
          ],
        },
        {
          header: 'Accesorios',
          items: [
            { label: 'Medias', route: '/hombre/accesorios/medias' },
            { label: 'Pulseras', route: '/hombre/accesorios/pulseras' },
            { label: 'Corbatas', route: '/hombre/accesorios/corbatas' },
            { label: 'Cinturones', route: '/hombre/accesorios/cinturones' },
          ],
        },
      ],
    },
  ];

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.isScrolled = window.scrollY > 20;
  }

  showMenu(menu: string) {
    this.activeMenu = menu;
  }

  hideMenu() {
    this.activeMenu = null;
  }

  isMenuActive(menu: string): boolean {
    return this.activeMenu === menu;
  }

  openAuthModal() {
    this.authModalService.open('login');
  }

  toggleUserMenu() {
    this.showUserMenu = !this.showUserMenu;
  }

  closeUserMenu() {
    this.showUserMenu = false;
  }

  logout() {
    this.authService.logout();
    this.closeUserMenu();
    this.router.navigate(['/']);
  }
}
