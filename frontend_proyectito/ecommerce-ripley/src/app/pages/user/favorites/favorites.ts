import { Component, OnInit, signal, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { ProductCardComponent } from '../../../components/product-card/product-card';
import { ProductCardResponse, mapProductToCardResponse } from '../../../models/product.model';
import { FavoriteService } from '../../../services/favorite.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [DashboardSidebarComponent, ProductCardComponent, RouterModule],
  templateUrl: './favorites.html',
  styleUrls: ['./favorites.scss']
})
export class FavoritesComponent implements OnInit {
  loading = signal(false);
  favorites = signal<ProductCardResponse[]>([]);

  constructor(
    private favoriteService: FavoriteService,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadFavorites();
  }

  loadFavorites(): void {
    this.loading.set(true);
    this.favoriteService.getFavorites().subscribe({
      next: (res) => {
        if (res.result) {
          const uniqueIds = new Set();
          const uniqueFavs = res.data.filter((fav: any) => {
            if (!fav.product) return false;
            if (uniqueIds.has(fav.product.id)) return false;
            uniqueIds.add(fav.product.id);
            return true;
          });
          const favProducts = uniqueFavs.map((fav: any) => {
            const p = fav.product;
            let img = '';
            if (p.variantes && p.variantes.length > 0 && p.variantes[0].imageUrls && p.variantes[0].imageUrls.length > 0) {
              img = p.variantes[0].imageUrls[0];
            }
            return {
              id: p.id,
              title: p.title ?? p.nombre ?? 'Producto sin nombre',
              marca: p.marca ?? '',
              price: p.price ?? p.precio ?? 0,
              discountedPrice: p.discountedPrice ?? p.precioAnterior ?? 0,
              discountPercent: p.discountPercent ?? p.descuento ?? 0,
              nuevo: p.nuevo ?? false,
              mainImageUrl: img,
              availableColors: p.variantes ? p.variantes.map((v:any) => v.color?.nombre || v.color) : [],
              stock: p.stock ?? 0
            } as ProductCardResponse;
          });
          this.favorites.set(favProducts);
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.toastService.error('Error', 'No se pudieron cargar los favoritos');
        this.loading.set(false);
      }
    });
  }

  removeFavorite(productId: number): void {
    this.favoriteService.removeFavorite(productId).subscribe({
      next: (res) => {
        if(res.result) {
          this.toastService.success('Favoritos', 'Producto eliminado de favoritos');
          this.loadFavorites();
        }
      },
      error: () => this.toastService.error('Error', 'No se pudo eliminar de favoritos')
    });
  }

  clearAllFavorites(): void {
    if (confirm('¿Eliminar todos los favoritos?')) {
      const currentFavs = this.favorites();
      // To simplify, we don't have a clear all endpoint, so we can just delete them one by one or ignore this functionality, 
      // but let's implement a loop or just remove the button from UI since it's not strictly necessary.
      // I'll leave the button but it will iterate.
      let count = 0;
      currentFavs.forEach(f => {
        this.favoriteService.removeFavorite(f.id).subscribe(() => {
          count++;
          if(count === currentFavs.length) {
            this.favorites.set([]);
            this.toastService.success('Favoritos', 'Todos los favoritos eliminados');
          }
        });
      });
    }
  }
}