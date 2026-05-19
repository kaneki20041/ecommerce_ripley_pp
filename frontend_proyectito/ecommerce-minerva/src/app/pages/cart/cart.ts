import { Component, computed, inject } from '@angular/core';
import { Product } from '../../models/product.model';
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
  // Productos recomendados (UI placeholder)
  recommendedProducts: Product[] = [];
  ngOnInit() {
    this.recommendedProducts = [
      {
        id: 999,
        nombre: 'Zapatillas Urban Runner',
        precio: 85.50,
        imagenes: ['https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&q=80'],
        stock: 10,
        colores: [{ nombre: 'Negro', hex: '#000000' }],
        tallas: ['S', 'M', 'L'],
        marca: 'Nike',
        descripcion: 'Zapatillas urbanas con diseño moderno y confort superior.',
        categoria: 'Calzado',
        genero: 'Unisex',
        nuevo: true,
        destacado: false,
        rating: 4.5,
        reviews: 0
      },
      {
        id: 888,
        nombre: 'Polo Classic Fit',
        precio: 120.00,
        imagenes: ['https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=400&q=80'],
        stock: 5,
        colores: [{ nombre: 'Blanco', hex: '#ffffff' }],
        tallas: ['M'],
        marca: 'Zara',
        descripcion: 'Polo de algodón premium con corte clásico.',
        categoria: 'Ropa',
        genero: 'Unisex',
        nuevo: false,
        destacado: true,
        rating: 5.0,
        reviews: 0
      },
      {
        id: 777,
        nombre: 'Mochila Trail Pro',
        precio: 65.00,
        imagenes: ['https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400&q=80'],
        stock: 8,
        colores: [{ nombre: 'Gris', hex: '#6b7280' }],
        tallas: ['Único'],
        marca: 'Adidas',
        descripcion: 'Mochila resistente al agua ideal para aventuras urbanas.',
        categoria: 'Accesorios',
        genero: 'Unisex',
        nuevo: false,
        destacado: false,
        rating: 4.0,
        reviews: 0
      }
    ];
  }
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

  addRecommendedToCart(product: Product): void {
    // Simple add with default size/color (first available)
    const size = product.tallas?.[0] || '';
    const color = product.colores?.[0]?.nombre || '';
    this.cartService.addToCart(product, 1, size, color);
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

  scrollCarousel(direction: number): void {
    const track = document.getElementById('carousel-track');
    if (track) {
      const cardWidth = 280; // ancho de card + gap
      track.scrollBy({ left: direction * cardWidth, behavior: 'smooth' });
    }
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