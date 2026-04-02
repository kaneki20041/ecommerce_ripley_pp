import { Injectable, signal, computed } from '@angular/core';
import { Product } from '../models/product.model';

export interface CartItem {
  product: Product;
  quantity: number;
  selectedSize: string;
  selectedColor: string;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  // Señal privada para los items del carrito
  private cartItemsSignal = signal<CartItem[]>([]);

  // Señales computadas públicas
  cartItems = this.cartItemsSignal.asReadonly();

  cartCount = computed(() => {
    return this.cartItemsSignal().reduce((total, item) => total + item.quantity, 0);
  });

  cartTotal = computed(() => {
    return this.cartItemsSignal().reduce((total, item) => {
      return total + (item.product.precio * item.quantity);
    }, 0);
  });

  cartSubtotal = computed(() => this.cartTotal());

  shipping = signal(0); // Envío se calculará según dirección

  cartGrandTotal = computed(() => {
    return this.cartTotal() + this.shipping();
  });

  constructor() {
    // Cargar carrito desde localStorage al iniciar
    this.loadCartFromStorage();
  }

  /**
   * Agregar producto al carrito
   */
  addToCart(product: Product, quantity: number = 1, size: string, color: string): void {
    const items = this.cartItemsSignal();

    // Buscar si el producto ya existe con la misma talla y color
    const existingItemIndex = items.findIndex(item =>
      item.product.id === product.id &&
      item.selectedSize === size &&
      item.selectedColor === color
    );

    if (existingItemIndex > -1) {
      // Si existe, aumentar cantidad
      const updatedItems = [...items];
      updatedItems[existingItemIndex].quantity += quantity;
      this.cartItemsSignal.set(updatedItems);
    } else {
      // Si no existe, agregar nuevo item
      const newItem: CartItem = {
        product,
        quantity,
        selectedSize: size,
        selectedColor: color
      };
      this.cartItemsSignal.set([...items, newItem]);
    }

    this.saveCartToStorage();
  }

  /**
   * Actualizar cantidad de un item
   */
  updateQuantity(productId: number, size: string, color: string, quantity: number): void {
    if (quantity <= 0) {
      this.removeFromCart(productId, size, color);
      return;
    }

    const items = this.cartItemsSignal();
    const updatedItems = items.map(item => {
      if (item.product.id === productId &&
        item.selectedSize === size &&
        item.selectedColor === color) {
        return { ...item, quantity };
      }
      return item;
    });

    this.cartItemsSignal.set(updatedItems);
    this.saveCartToStorage();
  }

  /**
   * Remover producto del carrito
   */
  removeFromCart(productId: number, size: string, color: string): void {
    const items = this.cartItemsSignal();
    const updatedItems = items.filter(item =>
      !(item.product.id === productId &&
        item.selectedSize === size &&
        item.selectedColor === color)
    );

    this.cartItemsSignal.set(updatedItems);
    this.saveCartToStorage();
  }

  /**
   * Limpiar todo el carrito
   */
  clearCart(): void {
    this.cartItemsSignal.set([]);
    this.saveCartToStorage();
  }

  /**
   * Verificar si un producto está en el carrito
   */
  isInCart(productId: number, size?: string, color?: string): boolean {
    const items = this.cartItemsSignal();

    if (size && color) {
      return items.some(item =>
        item.product.id === productId &&
        item.selectedSize === size &&
        item.selectedColor === color
      );
    }

    return items.some(item => item.product.id === productId);
  }

  /**
   * Obtener cantidad de un producto en el carrito
   */
  getProductQuantity(productId: number, size?: string, color?: string): number {
    const items = this.cartItemsSignal();

    if (size && color) {
      const item = items.find(item =>
        item.product.id === productId &&
        item.selectedSize === size &&
        item.selectedColor === color
      );
      return item?.quantity || 0;
    }

    // Sumar todas las cantidades de este producto (todas las variantes)
    return items
      .filter(item => item.product.id === productId)
      .reduce((total, item) => total + item.quantity, 0);
  }

  /**
   * Calcular descuento total
   */
  getTotalDiscount(): number {
    return this.cartItemsSignal().reduce((total, item) => {
      if (item.product.descuento && item.product.precioAnterior) {
        const discount = (item.product.precioAnterior - item.product.precio) * item.quantity;
        return total + discount;
      }
      return total;
    }, 0);
  }

  /**
   * Guardar carrito en localStorage
   */
  private saveCartToStorage(): void {
    try {
      const cartData = JSON.stringify(this.cartItemsSignal());
      localStorage.setItem('cart', cartData);
    } catch (error) {
      console.error('Error al guardar carrito:', error);
    }
  }

  /**
   * Cargar carrito desde localStorage
   */
  private loadCartFromStorage(): void {
    try {
      const cartData = localStorage.getItem('cart');
      if (cartData) {
        const items = JSON.parse(cartData);
        this.cartItemsSignal.set(items);
      }
    } catch (error) {
      console.error('Error al cargar carrito:', error);
      this.cartItemsSignal.set([]);
    }
  }
}