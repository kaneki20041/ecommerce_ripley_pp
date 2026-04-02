import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Product } from '../../models/product.model';
import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-card.html',
  styleUrls: ['./product-card.scss']
})
export class ProductCardComponent {
  @Input() product!: Product;
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
    const size = this.product.tallas[0];
    const color = this.product.colores[0].nombre;
    this.cartService.addToCart(this.product, 1, size, color);
  }

  getDiscountPercentage(): number {
    if (!this.product.descuento) return 0;
    return Math.round(this.product.descuento);
  }

  getMainImage(): string {
    return this.product.imagenes[0] || 'assets/images/placeholder.jpg';
  }

  isInCart(): boolean {
    return this.cartService.isInCart(this.product.id);
  }

  getRatingStars(): number[] {
    return Array(5).fill(0).map((_, i) => i);
  }
}