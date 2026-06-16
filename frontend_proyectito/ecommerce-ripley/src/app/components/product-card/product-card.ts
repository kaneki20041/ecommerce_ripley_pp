import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProductCardResponse } from '../../models/product.model';
import { CartService } from '../../services/cart.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-card.html',
  styleUrls: ['./product-card.scss']
})
export class ProductCardComponent {
  @Input() product!: ProductCardResponse;
  @Input() showQuickAdd: boolean = true;
  @Output() productClick = new EventEmitter<number>();

  constructor(
    private router: Router,
    private cartService: CartService
  ) { }

  viewProduct(): void {
    this.productClick.emit(this.product.id);
    this.router.navigate(['/producto', this.product.id]);
  }

  quickAddToCart(event: Event): void {
    event.stopPropagation();
    if (this.isAtStockLimit()) return;
    // availableColors now contains color NAMES (e.g. 'Negro', 'Blanco') since the mapping fix
    const size = 'Única'; // Default size for quick add (user can choose in product detail)
    const color = this.product.availableColors && this.product.availableColors.length > 0
                    ? this.product.availableColors[0] : '';
    
    this.cartService.addToCart(this.product as any, 1, size, color);
  }

  /** Returns true when the item already in cart has reached the stock limit */
  isAtStockLimit(): boolean {
    const p = this.product as any;
    const size = 'Única';
    const color = this.product.availableColors && this.product.availableColors.length > 0
                    ? this.product.availableColors[0] : '';

    let stockLimit = 99;
    // Try to get stock from variants
    if (p.variantes && Array.isArray(p.variantes) && p.variantes.length > 0) {
      const variant = p.variantes.find((v: any) => {
        const vSize = (v.talla && typeof v.talla === 'object') ? v.talla.valor : (v.size || v.talla || 'Única');
        const vColor = (v.color && typeof v.color === 'object') ? v.color.nombre : (v.color || 'Único');
        return vSize.toLowerCase() === size.toLowerCase() && vColor.toLowerCase() === color.toLowerCase();
      });
      if (variant) stockLimit = variant.stock;
    } else if (p.stock !== undefined) {
      stockLimit = p.stock;
    }

    const currentQty = this.cartService.getProductQuantity(this.product.id, size, color);
    return currentQty >= stockLimit;
  }

  getMainImage(): string {
    if (!this.product.mainImageUrl) return 'assets/images/placeholder.jpg';
    if (this.product.mainImageUrl.startsWith('/uploads') || this.product.mainImageUrl.startsWith('uploads')) {
      const path = this.product.mainImageUrl.startsWith('/') ? this.product.mainImageUrl : '/' + this.product.mainImageUrl;
      return `${environment.apiUrl}${path}`;
    }
    return this.product.mainImageUrl;
  }

  isInCart(): boolean {
    return this.cartService.isInCart(this.product.id);
  }

  getColorHex(colorName: string): string {
    const colorMap: { [key: string]: string } = {
      'blanco': '#FFFFFF',
      'negro': '#000000',
      'rojo': '#E53E3E',
      'azul': '#3182CE',
      'azul marino': '#00205B',
      'marino': '#00205B',
      'verde': '#38A169',
      'gris': '#718096',
      'gris oscuro': '#2D3748',
      'amarillo': '#ECC94B',
      'naranja': '#ED8936',
      'rosa': '#ED64A6',
      'lila': '#9F7AEA',
      'morado': '#805AD5',
      'celeste': '#63B3ED',
      'beige': '#F5F0E8',
      'marron': '#8B4513',
      'marrón': '#8B4513',
      'cafe': '#A0522D',
      'café': '#A0522D',
      'turquesa': '#38B2AC',
      'dorado': '#D4AF37',
      'plateado': '#A0AEC0',
    };
    return colorMap[colorName.toLowerCase().trim()] || '#CCCCCC';
  }
}