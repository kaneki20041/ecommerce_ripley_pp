import { Component, computed, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { Product, ProductCardResponse } from '../../models/product.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CartService, CartItem } from '../../services/cart.service';
import { AuthService } from '../../services/auth';
import { ProductService } from '../../services/product.service';
import { AuthModalService } from '../../services/auth-modal';
import { CouponService } from '../../services/coupon.service';
import { SmartCartService } from '../../services/smart-cart.service';
import { environment } from '../../../environments/environment';
import { lastValueFrom } from 'rxjs';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cart.html',
  styleUrls: ['./cart.scss']
})

export class CartComponent implements OnInit {
  cartService = inject(CartService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private productService = inject(ProductService);
  private authModalService = inject(AuthModalService);
  private couponServiceBackend = inject(CouponService);
  private smartCartService = inject(SmartCartService);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  upsellDiscountPercentage: number = 10; // Default fallback
  upsellActive: boolean = true;

  // Estado de sincronización con el backend
  syncingCart = false;

  // Estados para mini-dialogs premium
  showLoginModal = false;
  showDeleteConfirmModal = false;
  showClearConfirmModal = false;
  itemToDelete: CartItem | null = null;

  cartItems = this.cartService.cartItems;
  cartCount = this.cartService.cartCount;
  cartSubtotal = this.cartService.cartSubtotal;
  shipping = this.cartService.shipping;
  cartTotal = this.cartService.cartGrandTotal;

  recommendedProducts: any[] = [];

  // Toast Notification
  showToastMessage = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  get couponCode() { return this.cartService.couponCode() || ''; }
  set couponCode(val: string) { /* local input uses a local var if needed, but let's use a local var for input */ }
  
  couponInput = '';
  
  get couponApplied() { return !!this.cartService.couponCode(); }
  get couponDiscount() { return this.cartService.couponDiscount(); }
  couponError = '';

  // Cupones del usuario
  userCoupons: any[] = [];
  showCouponsModal = false;

  showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.showToastMessage = true;
    setTimeout(() => {
      this.showToastMessage = false;
    }, 3500);
  }

  ngOnInit() {
    this.loadSmartCartConfig();
    if (this.authService.isAuthenticated()) {
      this.loadUserCoupons();
      if (this.cartCount() > 0) {
        this.syncCartToBackendThenLoadRecommendations();
      } else {
        this.loadRecommendations();
      }
    } else {
      this.loadRecommendations();
    }
  }

  loadSmartCartConfig() {
    this.smartCartService.getConfig().subscribe({
      next: (res) => {
        if (res.result && res.data) {
          this.upsellDiscountPercentage = res.data.upsellDiscountPercentage;
          this.upsellActive = res.data.active;
        }
      },
      error: (err) => console.warn('Error loading SmartCart config:', err)
    });
  }

  /**
   * Sincroniza el carrito local (localStorage) con el backend de forma silenciosa.
   * Necesario para que la IA de Groq pueda leer los productos del carrito.
   * Se ejecuta en background al entrar al carrito cuando el usuario está autenticado.
   */
  async syncCartToBackendThenLoadRecommendations(): Promise<void> {
    this.syncingCart = true;
    const items = this.cartItems();
    console.log(`[Cart Sync] Sincronizando ${items.length} items al backend para habilitar IA...`);

    try {
      // 1. Limpiar el carrito de la base de datos primero
      try {
        await lastValueFrom(
          this.http.put(`${environment.apiUrl}/api/cart/clear`, {})
        );
        console.log('[Cart Sync] 🛒 Carrito de base de datos vaciado exitosamente antes de sincronizar para IA.');
      } catch (err) {
        console.warn('[Cart Sync] No se pudo vaciar el carrito en el backend:', err);
      }

      for (const item of items) {
        const p = item.product as any;
        const productId = p.id || p.productId;
        if (!productId) continue;
        await lastValueFrom(
          this.http.post(`${environment.apiUrl}/api/cart/add`, {
            productId: productId,
            size: item.selectedSize || 'Única',
            color: item.selectedColor || '',
            quantity: item.quantity
          })
        ).catch(err => {
          console.warn(`[Cart Sync] Item ${productId} falló silenciosamente:`, err?.status);
        });
      }
      console.log('[Cart Sync] ✅ Sincronización completada. Cargando recomendaciones de IA...');
    } catch (err) {
      console.error('[Cart Sync] ❌ Error durante la sincronización:', err);
    } finally {
      this.syncingCart = false;
      this.loadRecommendations();
    }
  }

  loadRecommendations() {
    const autenticado = this.authService.isAuthenticated();
    const cantidadCarrito = this.cartCount();
    console.log(`[IA Groq] Iniciando carga de recomendaciones | Autenticado: ${autenticado} | Items en carrito: ${cantidadCarrito}`);

    if (autenticado && cantidadCarrito > 0) {
      console.log('[IA Groq] ✅ Condiciones cumplidas. Enviando petición a /api/recomendaciones/carrito...');
      this.productService.getCartRecommendations().subscribe({
        next: (response) => {
          console.log('[IA Groq] ✅ Respuesta recibida del backend:', response);
          if (response && response.data && response.data.length > 0) {
            // Filtrar productos que tengan stock > 0
            this.recommendedProducts = response.data.filter((p: any) => p.stock > 0);
            
            console.log(`[IA Groq] ✅ ${this.recommendedProducts.length} recomendaciones de IA con stock cargadas exitosamente.`);
            
            if (this.recommendedProducts.length === 0) {
                console.warn('[IA Groq] ⚠️ Todos los productos recomendados estaban sin stock. Usando fallback.');
                this.loadFallbackRecommendations();
                return;
            }
            
            // Registrar Impresiones para cada producto recomendado
            this.recommendedProducts.forEach(prod => {
              if (prod && prod.id) {
                this.http.post(`${environment.apiUrl}/api/metrics/recommendations/${prod.id}/impression`, {}).subscribe({
                  error: (err) => console.warn(`Error al registrar impresión del producto ${prod.id}:`, err)
                });
              }
            });
          } else {
            console.warn('[IA Groq] ⚠️ Respuesta vacía o sin datos. Usando fallback con productos destacados.');
            this.loadFallbackRecommendations();
          }
        },
        error: (err) => {
          console.error('[IA Groq] ❌ Error al llamar al backend:', err);
          console.error('[IA Groq] ❌ Status:', err.status, '| Message:', err.message);
          this.loadFallbackRecommendations();
        }
      });
    } else {
      console.warn(`[IA Groq] ⚠️ No se llamará a la IA. Autenticado: ${autenticado}, Items: ${cantidadCarrito}. Usando fallback.`);
      this.loadFallbackRecommendations();
    }
  }

  loadFallbackRecommendations() {
    this.productService.getFeaturedProducts().subscribe({
      next: (products) => {
        // Filtrar productos con stock > 0
        this.recommendedProducts = products.filter((p: any) => p.stock > 0).slice(0, 6);
      },
      error: (err) => {
        console.error('Error al cargar productos destacados de contingencia:', err);
      }
    });
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
    const stock = this.getVariantStock(item);
    if (item.quantity < stock) {
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
    this.itemToDelete = item;
    this.showDeleteConfirmModal = true;
  }

  confirmDelete(): void {
    if (this.itemToDelete) {
      this.cartService.removeFromCart(
        this.itemToDelete.product.id,
        this.itemToDelete.selectedSize,
        this.itemToDelete.selectedColor
      );
      this.showDeleteConfirmModal = false;
      this.itemToDelete = null;
    }
  }

  cancelDelete(): void {
    this.showDeleteConfirmModal = false;
    this.itemToDelete = null;
  }

  // Limpiar carrito
  clearCart(): void {
    this.showClearConfirmModal = true;
  }

  confirmClear(): void {
    this.cartService.clearCart();
    this.showClearConfirmModal = false;
  }

  cancelClear(): void {
    this.showClearConfirmModal = false;
  }

  // Cupón de descuento
  applyCoupon(): void {
    this.couponError = '';

    if (!this.couponInput.trim()) {
      this.couponError = 'Ingresa un código de cupón';
      return;
    }

    this.couponServiceBackend.validateCoupon(this.couponInput, this.cartTotal()).subscribe({
      next: (res) => {
        if (res.result && res.data) {
          this.cartService.couponCode.set(res.data.code);
          this.cartService.couponDiscount.set(res.data.discount);
          
          // Re-save cart state to storage so coupon is persisted
          (this.cartService as any).saveCartToStorage();

          this.showToast(`¡Cupón ${res.data.code} aplicado con éxito!`, 'success');
          this.couponInput = '';
        } else {
          this.couponError = res.message || 'Cupón inválido o expirado';
          this.showToast(this.couponError, 'error');
        }
      },
      error: (err) => {
        this.couponError = err.error?.message || 'Error al validar cupón';
        this.showToast(this.couponError, 'error');
      }
    });
  }

  loadUserCoupons(): void {
    this.couponServiceBackend.getMyCoupons().subscribe({
      next: (res) => {
        if (res.result && res.data) {
          this.userCoupons = res.data.filter(c => c.status === 'activo');
        }
      },
      error: (err) => console.error('Error loading user coupons', err)
    });
  }

  openCouponsModal(): void {
    this.showCouponsModal = true;
  }

  closeCouponsModal(): void {
    this.showCouponsModal = false;
  }

  selectCoupon(code: string): void {
    this.couponInput = code;
    this.applyCoupon();
    this.closeCouponsModal();
  }

  removeCoupon(): void {
    this.cartService.couponCode.set(null);
    this.cartService.couponDiscount.set(0);
    this.couponError = '';
    (this.cartService as any).saveCartToStorage();
  }

  // Calcular total con cupón
  getFinalTotal(): number {
    return this.cartService.cartGrandTotal();
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

  goToRecommendedProduct(product: any): void {
    if (product && product.id) {
      // Registrar Click en la recomendación
      this.http.post(`${environment.apiUrl}/api/metrics/recommendations/${product.id}/click`, {}).subscribe({
        error: (err) => console.warn(`Error al registrar click del producto ${product.id}:`, err)
      });
      // Navegar al detalle para selección segura de talla, color y stock
      this.router.navigate([`/producto/${product.id}`]);
    }
  }
  showUpsellModal: boolean = false;

  // Countdown logic
  showCheckoutCountdown: boolean = false;
  checkoutSecondsLeft: number = 120; // 2 minutos
  private countdownInterval: any;

  proceedToCheckout(): void {
    if (this.authService.isAuthenticated()) {
      if (this.upsellActive && this.recommendedProducts && this.recommendedProducts.length > 0) {
        this.showUpsellModal = true;
      } else {
        this.startCheckoutCountdown();
      }
    } else {
      this.showLoginModal = true;
    }
  }

  continueToCheckoutAfterUpsell(): void {
    this.showUpsellModal = false;
    this.registerIgnoredCategories();
    this.startCheckoutCountdown();
  }

  startCheckoutCountdown(): void {
    this.showCheckoutCountdown = true;
    this.checkoutSecondsLeft = 120;
    
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
    
    this.countdownInterval = setInterval(() => {
      this.checkoutSecondsLeft--;
      this.cdr.detectChanges(); // Trigger change detection for visual updates
      if (this.checkoutSecondsLeft <= 0) {
        this.executeCheckoutRedirect();
      }
    }, 1000);
  }

  cancelCheckoutRedirect(): void {
    this.showCheckoutCountdown = false;
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
  }

  executeCheckoutRedirect(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
    this.showCheckoutCountdown = false;
    this.router.navigate(['/checkout']);
  }

  formatTimeLeft(): string {
    const minutes = Math.floor(this.checkoutSecondsLeft / 60);
    const seconds = this.checkoutSecondsLeft % 60;
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  registerIgnoredCategories(): void {
    const ignored: string[] = [];
    this.recommendedProducts.forEach(prod => {
      if (prod.categoryName && !ignored.includes(prod.categoryName)) {
        ignored.push(prod.categoryName);
      }
    });
    if (ignored.length > 0) {
      sessionStorage.setItem('ignoredCategories', JSON.stringify(ignored));
      console.log('[Feedback Implícito] Categorías ignoradas guardadas:', ignored);
    }
  }

  ignoreProduct(product: ProductCardResponse): void {
    if (product.categoryName) {
      let ignoredStr = sessionStorage.getItem('ignoredCategories');
      let ignored: string[] = ignoredStr ? JSON.parse(ignoredStr) : [];
      if (!ignored.includes(product.categoryName)) {
        ignored.push(product.categoryName);
        sessionStorage.setItem('ignoredCategories', JSON.stringify(ignored));
      }
    }
    // Remove from UI
    this.recommendedProducts = this.recommendedProducts.filter(p => p.id !== product.id);
  }

  addUpsellToCart(product: ProductCardResponse): void {
    // Collect sizes and colors currently in cart to prioritize them
    const cartSizes = [...new Set(this.cartItems().map(item => item.selectedSize))];
    const cartColors = [...new Set(this.cartItems().map(item => item.selectedColor))];

    // Helper to pick a varied item, prioritizing matches from cart
    const pickVaried = (available: string[] | undefined, preferred: string[], fallback: string) => {
      if (!available || available.length === 0) return fallback;
      const matches = available.filter(v => preferred.includes(v));
      if (matches.length > 0) {
        // Pick random from matches
        return matches[Math.floor(Math.random() * matches.length)];
      }
      // Pick random from available
      return available[Math.floor(Math.random() * available.length)];
    };

    let size = product.suggestedSize || pickVaried(product.availableSizes, cartSizes, 'Única');
    let color = pickVaried(product.availableColors, cartColors, 'Único');

    const req = {
      productId: product.id,
      size: size,
      color: color,
      quantity: 1
    };

    const token = localStorage.getItem('jwt');
    const headers = token ? { Authorization: `Bearer ${token}` } : undefined;

    this.http.post(`${environment.apiUrl}/api/cart/add-upsell`, req, { headers }).subscribe({
      next: () => {
        // Obtenemos el producto completo con sus variantes para validar el stock real en el frontend
        this.productService.getProductById(product.id).subscribe({
          next: (fullProduct) => {
            if (fullProduct) {
              const p: any = { ...fullProduct };
              p.precioAnterior = p.precio;
              p.precio = Math.round(p.precio * (1 - (this.upsellDiscountPercentage / 100)));
              this.cartService.addToCart(p, 1, size, color);
            }
            this.showToast(`¡Añadido al carrito con ${this.upsellDiscountPercentage}% de descuento!`, 'success');
            // Ya no lo removemos del modal para que el usuario siga viéndolo
            // Recargar el carrito de recomendaciones para reflejar cambios
            this.loadRecommendations();
          },
          error: (err) => {
            console.error('Error fetching full product', err);
            // Si falla, añadirlo igual pero sin límite de stock (fallback)
            const p: any = { ...product };
            p.precioAnterior = p.price;
            p.precio = Math.round(p.price * (1 - (this.upsellDiscountPercentage / 100))); 
            p.stock = 99;
            this.cartService.addToCart(p, 1, size, color);
            this.showToast(`¡Añadido al carrito con ${this.upsellDiscountPercentage}% de descuento!`, 'success');
            this.loadRecommendations();
          }
        });
      },
      error: (err) => {
        console.error('Error adding upsell to cart', err);
        this.showToast('Hubo un error al agregar el producto. Quizá no hay stock en esa variante.', 'error');
      }
    });
  }

  confirmLogin(): void {
    this.showLoginModal = false;
    this.authModalService.open('login');
  }

  cancelLogin(): void {
    this.showLoginModal = false;
  }

  viewProduct(productId: number): void {
    // Registrar Click en la recomendación si es parte de las recomendadas
    const isRecommended = this.recommendedProducts.some(p => p.id === productId);
    if (isRecommended) {
      this.http.post(`${environment.apiUrl}/api/metrics/recommendations/${productId}/click`, {}).subscribe({
        error: (err) => console.warn(`Error al registrar click del producto ${productId}:`, err)
      });
    }
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
    return this.getItemPrice(item) * item.quantity;
  }

  getItemPrice(item: CartItem): number {
    const p = item.product as any;
    return p.precio !== undefined ? p.precio : (p.price !== undefined ? p.price : 0);
  }

  getItemOldPrice(item: CartItem): number | undefined {
    const p = item.product as any;
    return p.precioAnterior !== undefined ? p.precioAnterior : (p.discountedPrice !== undefined && p.discountPercent > 0 ? p.price : undefined);
  }

  getItemTitle(item: CartItem): string {
    const p = item.product as any;
    return p.nombre || p.title || 'Producto sin nombre';
  }

  formatImageUrl(url: string): string {
    if (!url) return 'https://simple.ripley.com.pe/static/img/ripley-logo.svg';
    if (url.startsWith('/uploads') || url.startsWith('uploads')) {
      return `${environment.apiUrl}${url.startsWith('/') ? url : '/' + url}`;
    }
    return url;
  }

  getItemImage(item: CartItem): string {
    const p = item.product as any;
    if (!p) return 'https://simple.ripley.com.pe/static/img/ripley-logo.svg';

    let imgUrl = '';
    const selectedColor = item.selectedColor;

    // 1. Intentar buscar en variantes (Estructura de la Entidad JPA de Spring Boot)
    if (p.variantes && Array.isArray(p.variantes) && p.variantes.length > 0) {
      let variant = p.variantes.find((v: any) => v.color && v.color.nombre && selectedColor && v.color.nombre.toLowerCase() === selectedColor.toLowerCase());
      if (!variant) {
        variant = p.variantes[0];
      }
      if (variant && variant.imageUrls && variant.imageUrls.length > 0) {
        imgUrl = variant.imageUrls[0];
      }
    }

    // 2. Intentar buscar en colores del mock
    if (!imgUrl && p.colores && Array.isArray(p.colores)) {
      const colorObj = p.colores.find((c: any) => selectedColor && c.nombre && c.nombre.toLowerCase() === selectedColor.toLowerCase());
      if (colorObj && colorObj.imagenes && colorObj.imagenes.length > 0) {
        imgUrl = colorObj.imagenes[0];
      }
    }

    // 3. Fallbacks
    if (!imgUrl && p.imagenes && p.imagenes.length > 0) imgUrl = p.imagenes[0];
    if (!imgUrl && p.mainImageUrl) imgUrl = p.mainImageUrl;
    
    if (!imgUrl) return 'https://simple.ripley.com.pe/static/img/ripley-logo.svg';

    return this.formatImageUrl(imgUrl);
  }

  getColorHex(item: CartItem): string {
    const p = item.product as any;
    // Si la estructura es de Product normal
    if (p.colores && Array.isArray(p.colores)) {
      const color = p.colores.find((c: any) => c.nombre === item.selectedColor);
      return color?.hexCode || '#000000';
    }
    // Para ProductCardResponse o datos simplificados, mapeo básico (idealmente esto viene del backend)
    const colorMap: {[key:string]:string} = {
      'Negro': '#000000', 'Blanco': '#FFFFFF', 'Rojo': '#FF0000', 
      'Azul': '#0000FF', 'Gris': '#808080'
    };
    return colorMap[item.selectedColor] || '#000000';
  }

  // Helpers para productos recomendados
  getRecTitle(prod: any): string {
    return prod.nombre || prod.title || 'Producto';
  }

  getRecPrice(prod: any): number {
    return prod.precio !== undefined ? prod.precio : (prod.price !== undefined ? prod.price : 0);
  }

  getRecImage(prod: any): string {
    // Primero intentar imagen del array de imágenes (para Product completo)
    if (prod.imagenes && prod.imagenes.length > 0) {
      const img = prod.imagenes[0];
      return this.resolveImageUrl(img);
    }
    // Para ProductCardResponse (viene de la IA/backend)
    if (prod.mainImageUrl) {
      return this.resolveImageUrl(prod.mainImageUrl);
    }
    return 'assets/placeholder.png';
  }

  /**
   * Resuelve la URL de una imagen: si es una ruta de uploads del backend, 
   * añade la URL base de la API. Si es una URL completa (http/https), la usa tal cual.
   */
  resolveImageUrl(imageUrl: string): string {
    if (!imageUrl) return 'assets/placeholder.png';
    // Ya es URL absoluta (https:// o http://)
    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
      return imageUrl;
    }
    // Es una ruta de uploads del backend → añadir base URL
    if (imageUrl.startsWith('uploads/') || imageUrl.startsWith('/uploads/')) {
      const path = imageUrl.startsWith('/') ? imageUrl : '/' + imageUrl;
      return `${environment.apiUrl}${path}`;
    }
    return imageUrl;
  }

  getVariantStock(item: CartItem): number {
    const p = item.product as any;
    if (!p) return 0;
    if (!p.variantes || !Array.isArray(p.variantes) || p.variantes.length === 0) {
      return p.stock !== undefined ? p.stock : 99;
    }
    const size = item.selectedSize;
    const color = item.selectedColor;
    const variant = p.variantes.find((v: any) => {
      const vSize = (v.talla && typeof v.talla === 'object') ? v.talla.valor : (v.size || v.talla || 'Única');
      const vColor = (v.color && typeof v.color === 'object') ? v.color.nombre : (v.color || 'Único');
      return vSize.toLowerCase() === size.toLowerCase() && vColor.toLowerCase() === color.toLowerCase();
    });
    return variant ? variant.stock : 0;
  }

}