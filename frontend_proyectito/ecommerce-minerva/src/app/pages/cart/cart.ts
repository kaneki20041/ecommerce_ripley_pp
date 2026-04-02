import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService, CartItem } from '../../services/cart.service';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cart.html',
  styleUrls: ['./cart.scss']
})
export class CartComponent {
  cartService = inject(CartService);
  private authService = inject(AuthService);
  private router = inject(Router);

  cartItems = this.cartService.cartItems;
  cartCount = this.cartService.cartCount;
  cartSubtotal = this.cartService.cartSubtotal;
  shipping = this.cartService.shipping;
  cartTotal = this.cartService.cartGrandTotal;

  // Cupón de descuento
  couponCode = '';
  couponApplied = false;
  couponDiscount = 0;
  couponError = '';

  // Descuento total de productos
  productsDiscount = computed(() => this.cartService.getTotalDiscount());

  constructor() {
    // Calcular envío (ejemplo: gratis si es mayor a 150)
    if (this.cartSubtotal() >= 150) {
      this.cartService.shipping.set(0);
    } else {
      this.cartService.shipping.set(15);
    }
  }

  // Actualizar cantidad
  updateQuantity(item: CartItem, newQuantity: number): void {
    this.cartService.updateQuantity(
      item.product.id,
      item.selectedSize,
      item.selectedColor,
      newQuantity
    );
  }

  increaseQuantity(item: CartItem): void {
    if (item.quantity < item.product.stock) {
      this.updateQuantity(item, item.quantity + 1);
    }
  }

  decreaseQuantity(item: CartItem): void {
    if (item.quantity > 1) {
      this.updateQuantity(item, item.quantity - 1);
    }
  }

  // Remover item
  removeItem(item: CartItem): void {
    if (confirm('¿Deseas eliminar este producto del carrito?')) {
      this.cartService.removeFromCart(
        item.product.id,
        item.selectedSize,
        item.selectedColor
      );
    }
  }

  // Limpiar carrito
  clearCart(): void {
    if (confirm('¿Deseas vaciar todo el carrito?')) {
      this.cartService.clearCart();
    }
  }

  // Cupón de descuento
  applyCoupon(): void {
    this.couponError = '';

    if (!this.couponCode.trim()) {
      this.couponError = 'Ingresa un código de cupón';
      return;
    }

    // Simulación de cupones (conectar con backend después)
    const validCoupons: { [key: string]: number } = {
      'DESCUENTO10': 10,
      'PRIMERACOMPRA': 15,
      'VERANO2024': 20
    };

    const discount = validCoupons[this.couponCode.toUpperCase()];

    if (discount) {
      this.couponApplied = true;
      this.couponDiscount = discount;
    } else {
      this.couponError = 'Cupón inválido o expirado';
    }
  }

  removeCoupon(): void {
    this.couponCode = '';
    this.couponApplied = false;
    this.couponDiscount = 0;
    this.couponError = '';
  }

  // Calcular total con cupón
  getFinalTotal(): number {
    const total = this.cartTotal();
    if (this.couponApplied) {
      return total - (total * this.couponDiscount / 100);
    }
    return total;
  }

  getCouponAmount(): number {
    if (this.couponApplied) {
      return this.cartTotal() * this.couponDiscount / 100;
    }
    return 0;
  }

  // Navegación
  continueShopping(): void {
    this.router.navigate(['/productos']);
  }

  proceedToCheckout(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/checkout']);
    } else {
      // Mostrar modal de login (implementar después)
      alert('Debes iniciar sesión para continuar');
      // this.authModalService.open('login');
    }
  }

  viewProduct(productId: number): void {
    this.router.navigate(['/producto', productId]);
  }

  // Helpers
  getItemSubtotal(item: CartItem): number {
    return item.product.precio * item.quantity;
  }

  getColorHex(item: CartItem): string {
    const color = item.product.colores.find(c => c.nombre === item.selectedColor);
    return color?.hex || '#000000';
  }
}