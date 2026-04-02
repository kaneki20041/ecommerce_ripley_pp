import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { ProductCardComponent } from '../../../components/product-card/product-card';
import { Product } from '../../../models/product.model';
import { ProductService } from '../../../services/product.service';

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [DashboardSidebarComponent, ProductCardComponent],
  templateUrl: './favorites.html',
  styleUrls: ['./favorites.scss']
})
export class FavoritesComponent implements OnInit {
  loading = signal(false);
  favorites = signal<Product[]>([]);

  constructor(
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadFavorites();
  }

  loadFavorites(): void {
    this.loading.set(true);
    
    // Simular carga de favoritos (IDs guardados en localStorage)
    const favoriteIds = this.getFavoriteIds();
    
    this.productService.getAllProducts().subscribe({
      next: (products) => {
        const favProducts = products.filter(p => favoriteIds.includes(p.id));
        this.favorites.set(favProducts);
        this.loading.set(false);
      }
    });
  }

  getFavoriteIds(): number[] {
    const stored = localStorage.getItem('favorites');
    return stored ? JSON.parse(stored) : [1, 3, 5, 7]; // Mock IDs
  }

  removeFavorite(productId: number): void {
    const current = this.getFavoriteIds();
    const updated = current.filter(id => id !== productId);
    localStorage.setItem('favorites', JSON.stringify(updated));
    this.loadFavorites();
  }

  clearAllFavorites(): void {
    if (confirm('¿Eliminar todos los favoritos?')) {
      localStorage.removeItem('favorites');
      this.favorites.set([]);
    }
  }
}