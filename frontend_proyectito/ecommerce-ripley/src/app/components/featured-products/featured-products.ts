import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { environment } from '../../../environments/environment';

interface FeaturedProduct {
  id: number;
  name: string;
  category: string;
  price: number;
  oldPrice?: number;
  image: string;
  rating: number;
  reviews: number;
  isNew?: boolean;
  discount?: number;
}

@Component({
  selector: 'app-featured-products',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './featured-products.html',
  styleUrls: ['./featured-products.scss'],
})
export class FeaturedProductsComponent implements OnInit {
  products: FeaturedProduct[] = [];

  // Quick View Modal state
  quickViewProduct: any = null;
  quickViewLoading = false;
  quickViewSelectedSize = '';
  quickViewSelectedColor = '';
  quickViewQuantity = 1;
  quickViewAdded = false;

  constructor(
    private productService: ProductService,
    private router: Router,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    this.productService.getFeaturedProducts().subscribe({
      next: (realProducts) => {
        this.products = realProducts.slice(0, 8).map(p => ({
          id: p.id,
          name: p.nombre,
          category: p.categoria,
          price: p.precio,
          oldPrice: p.precioAnterior,
          image: p.imagenes && p.imagenes.length > 0 ? p.imagenes[0] : 'assets/images/placeholder.jpg',
          rating: p.rating || 4.5,
          reviews: p.reviews || 10,
          isNew: p.nuevo,
          discount: p.descuento
        }));
      },
      error: (err) => {
        console.error('Error fetching featured products:', err);
      }
    });
  }

  addToCart(product: FeaturedProduct) {
    this.router.navigate(['/producto', product.id]);
  }

  generateStars(rating: number): number[] {
    return Array(5)
      .fill(0)
      .map((_, i) => (i < Math.round(rating) ? 1 : 0));
  }

  // ─── Quick View ───────────────────────────────────────────────────────────

  openQuickView(event: Event, productId: number): void {
    event.stopPropagation();
    this.quickViewLoading = true;
    this.quickViewProduct = null;
    this.quickViewQuantity = 1;
    this.quickViewAdded = false;

    this.productService.getProductById(productId).subscribe({
      next: (p) => {
        if (p) {
          this.quickViewProduct = p;
          this.quickViewSelectedSize = p.tallas?.[0] || '';
          this.quickViewSelectedColor = p.colores?.[0]?.nombre || '';
        }
        this.quickViewLoading = false;
      },
      error: () => {
        this.quickViewLoading = false;
      }
    });
  }

  closeQuickView(): void {
    this.quickViewProduct = null;
    this.quickViewLoading = false;
    this.quickViewAdded = false;
    this.quickViewQuantity = 1;
  }

  qvSelectSize(size: string): void {
    this.quickViewSelectedSize = size;
  }

  qvSelectColor(colorName: string): void {
    this.quickViewSelectedColor = colorName;
  }

  qvGetVariantStock(): number {
    const p = this.quickViewProduct;
    if (!p) return 0;
    if (!p.variantes || p.variantes.length === 0) return p.stock ?? 99;
    const v = p.variantes.find((v: any) => {
      const vSize = (v.talla && typeof v.talla === 'object') ? v.talla.valor : (v.size || v.talla || 'Única');
      const vColor = (v.color && typeof v.color === 'object') ? v.color.nombre : (v.color || 'Único');
      return vSize?.toLowerCase() === this.quickViewSelectedSize?.toLowerCase()
          && vColor?.toLowerCase() === this.quickViewSelectedColor?.toLowerCase();
    });
    return v ? v.stock : 0;
  }

  qvIncreaseQty(): void {
    if (this.quickViewQuantity < this.qvGetVariantStock()) {
      this.quickViewQuantity++;
    }
  }

  qvDecreaseQty(): void {
    if (this.quickViewQuantity > 1) this.quickViewQuantity--;
  }

  qvAddToCart(): void {
    const p = this.quickViewProduct;
    if (!p || this.qvGetVariantStock() === 0) return;
    this.cartService.addToCart(p, this.quickViewQuantity, this.quickViewSelectedSize, this.quickViewSelectedColor);
    this.quickViewAdded = true;
    setTimeout(() => { this.quickViewAdded = false; }, 2000);
  }

  qvGoToProduct(): void {
    if (this.quickViewProduct) {
      this.router.navigate(['/producto', this.quickViewProduct.id]);
      this.closeQuickView();
    }
  }

  resolveImage(url: string): string {
    if (!url) return 'assets/images/placeholder.jpg';
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    const path = url.startsWith('/') ? url : '/' + url;
    return `${environment.apiUrl}${path}`;
  }

  qvGetCurrentImages(): string[] {
    const p = this.quickViewProduct;
    if (!p) return [];
    const colorObj = p.colores?.find((c: any) => c.nombre === this.quickViewSelectedColor);
    if (colorObj?.imagenes?.length) return colorObj.imagenes;
    // fallback to variante imageUrls
    const variant = p.variantes?.find((v: any) => {
      const vColor = (v.color && typeof v.color === 'object') ? v.color.nombre : v.color;
      return vColor?.toLowerCase() === this.quickViewSelectedColor?.toLowerCase();
    });
    if (variant?.imageUrls?.length) return variant.imageUrls;
    return p.imagenes || [];
  }

  qvGetColorHex(colorName: string): string {
    const p = this.quickViewProduct;
    if (!p) return '#ccc';
    const c = p.colores?.find((c: any) => c.nombre === colorName);
    return c?.hexCode || '#ccc';
  }
}
