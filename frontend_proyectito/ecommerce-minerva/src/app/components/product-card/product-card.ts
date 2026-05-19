import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProductCardResponse } from '../../models/product.model';
import { CartService } from '../../services/cart.service';

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
    // Since ProductCardResponse object no longer returns sizes/colors, we mock it or use 
    // real fields if the add to cart supports something simpler.
    const size = 'Única'; // Default as we don't have this in Card
    const color = this.product.availableColors && this.product.availableColors.length > 0
                    ? this.product.availableColors[0] : 'N/A';
    
    // We map ProductCardResponse to what cart expects loosely or wait until cart is updated.
    // Assuming cart accepts whatever object, or we cast.
    this.cartService.addToCart(this.product as any, 1, size, color);
  }

  getMainImage(): string {
    return this.product.mainImageUrl || 'assets/images/placeholder.jpg';
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