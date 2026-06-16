import { Component, HostListener, computed, inject, OnInit } from '@angular/core';

import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';
import { AuthModalService } from '../../services/auth-modal';
import { CartService } from '../../services/cart.service';
import { CategoryService } from '../../services/category.service';

interface SubMenuItem {
  label: string;
  route: string;
  queryParams?: any;
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
export class NavbarComponent implements OnInit {
  private cartService = inject(CartService);
  private categoryService = inject(CategoryService);
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

  ngOnInit(): void {
    this.categoryService.getPublicCategoryTree().subscribe({
      next: (res) => {
        if (res.result && res.data && res.data.length > 0) {
          this.menuItems = res.data.map(l1 => {
            return {
              label: l1.name,
              categories: l1.subcategories.map(l2 => {
                return {
                  header: l2.name,
                  items: l2.subcategories.map(l3 => {
                    const isGender = l1.name === 'Mujer' || l1.name === 'Hombre';
                    return {
                      label: l3.name,
                      route: '/productos',
                      queryParams: isGender 
                        ? { genero: l1.name, categoria: l3.name }
                        : { categoria: l3.name }
                    };
                  })
                };
              })
            };
          });
        }
      },
      error: (err) => {
        console.warn('Error fetching dynamic categories, using hardcoded fallback:', err);
      }
    });
  }

  menuItems: MenuItem[] = [
    {
      label: 'Mujer',
      categories: [
        {
          header: 'Ropa Mujer',
          items: [
            { label: 'Polos y tops', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Polos y tops' } },
            { label: 'Blusas', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Blusas' } },
            { label: 'Jeans', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Jeans' } },
            { label: 'Vestidos y enterizos', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Vestidos y enterizos' } },
            { label: 'Pantalones y joggers', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Pantalones y joggers' } },
            { label: 'Faldas', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Faldas' } },
            { label: 'Shorts', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Shorts' } },
            { label: 'Cárdigans', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Cárdigans' } },
            { label: 'Casacas', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Casacas' } },
            { label: 'Poleras', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Poleras' } },
            { label: 'Blazers', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Blazers' } },
          ],
        },
        {
          header: 'Lencería y ropa interior',
          items: [
            { label: 'Pijamas y camisones', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Pijamas y camisones' } },
            { label: 'Calzones', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Calzones' } },
            { label: 'Sostenes', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Sostenes' } },
            { label: 'Panties y medias', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Panties y medias' } },
            { label: 'Batas', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Batas' } },
            { label: 'Fajas y modeladores', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Fajas y modeladores' } },
          ],
        },
        {
          header: 'Ropa de baño',
          items: [
            { label: 'Bikinis', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Bikinis' } },
            { label: 'Ropa de baño entera', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Ropa de baño entera' } },
            { label: 'Accesorios de playa', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Accesorios de playa' } },
            { label: 'Pareos', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Pareos' } },
          ],
        },
      ],
    },
    {
      label: 'Calzado',
      categories: [
        {
          header: 'Zapatillas',
          items: [
            { label: 'Urbanas', route: '/productos', queryParams: { categoria: 'Zapatillas Urbanas' } },
            { label: 'Deportivas', route: '/productos', queryParams: { categoria: 'Zapatillas Deportivas' } },
            { label: 'Limpiadores de zapatillas', route: '/productos', queryParams: { categoria: 'Limpiadores de zapatillas' } },
          ],
        },
        {
          header: 'Zapatos mujer',
          items: [
            { label: 'Zapatillas urbanas', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Zapatillas Urbanas' } },
            { label: 'Zapatillas deportivas', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Zapatillas Deportivas' } },
            { label: 'Zapatos de vestir', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Zapatos de vestir' } },
            { label: 'Zapatos casuales', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Zapatos casuales' } },
            { label: 'Ballerinas', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Ballerinas' } },
            { label: 'Sandalias', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Sandalias' } },
            { label: 'Pantuflas', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Pantuflas' } },
            { label: 'Botas y botines', route: '/productos', queryParams: { genero: 'Mujer', categoria: 'Botas y botines' } },
          ],
        },
        {
          header: 'Zapatos hombre',
          items: [
            { label: 'Zapatillas urbanas', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Zapatillas Urbanas' } },
            { label: 'Zapatillas deportivas', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Zapatillas Deportivas' } },
            { label: 'Zapatos casuales', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Zapatos casuales' } },
            { label: 'Zapatos de vestir', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Zapatos de vestir' } },
            { label: 'Botines', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Botines' } },
            { label: 'Sandalias', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Sandalias' } },
            { label: 'Pantuflas', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Pantuflas' } },
          ],
        },
        {
          header: 'Zapatos niños y niñas',
          items: [
            { label: 'Calzado Escolar', route: '/productos', queryParams: { genero: 'Niños', categoria: 'Calzado Escolar' } },
            { label: 'Zapatillas de Futbol', route: '/productos', queryParams: { genero: 'Niños', categoria: 'Zapatillas de Futbol' } },
          ],
        }
      ],
    },
    {
      label: 'Hombre',
      categories: [
        {
          header: 'Ropa Hombre',
          items: [
            { label: 'Casacas y chalecos', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Casacas y chalecos' } },
            { label: 'Poleras y polerones', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Poleras y polerones' } },
            { label: 'Chompas', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Chompas' } },
            { label: 'Polos', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Polos' } },
            { label: 'Pantalones', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Pantalones' } },
            { label: 'Jeans', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Jeans' } },
            { label: 'Camisas', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Camisas' } },
            { label: 'Shorts', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Shorts' } },
            { label: 'Ropa de baño', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Ropa de baño' } },
          ],
        },
        {
          header: 'Ropa formal',
          items: [
            { label: 'Camisas de vestir', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Camisas de vestir' } },
            { label: 'Pantalones de vestir', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Pantalones de vestir' } },
            { label: 'Blazers y sacos', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Blazers y sacos' } },
            { label: 'Ternos', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Ternos' } },
            { label: 'Accesorios', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Accesorios formales' } },
          ],
        },
        {
          header: 'Ropa Interior',
          items: [
            { label: 'Bóxers y calzoncillos', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Bóxers y calzoncillos' } },
            { label: 'Pijamas', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Pijamas' } },
            { label: 'Medias', route: '/productos', queryParams: { genero: 'Hombre', categoria: 'Medias' } },
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
