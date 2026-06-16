import { Component, computed, inject, OnInit, signal, AfterViewInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CartService, CartItem } from '../../services/cart.service';
import { AuthService } from '../../services/auth';
import { OrderService, AddressRequest } from '../../services/order.service';
import { forkJoin, lastValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UBIGEO_DATA } from './ubigeo-data';

// Declarar la variable global del SDK de Mercado Pago (cargada en index.html)
declare const MercadoPago: any;

interface RegionData {
  departamento: string;
  provincias: {
    nombre: string;
    distritos: string[];
  }[];
}

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './checkout.html',
  styleUrls: ['./checkout.scss']
})
export class CheckoutComponent implements OnInit {
  cartService = inject(CartService);
  private authService = inject(AuthService);
  private orderService = inject(OrderService);
  private router = inject(Router);
  private http = inject(HttpClient);
  private ngZone = inject(NgZone);

  currentUser = this.authService.currentUser;

  // ============================
  // PASO DEL CHECKOUT: 1 = Entrega, 2 = Pago con Brick
  // ============================
  checkoutStep = signal<1 | 2>(1);
  createdOrderId = signal<number | null>(null);

  // Instancia del Payment Brick de MP
  private mpBrickController: any = null;

  // Formulario fields
  firstName = '';
  lastName = '';
  streetAddress = '';
  streetNumber = '';
  apartment = '';
  zipCode = '15000';
  celular = '';
  deliveryInstructions = '';
  addressName = '';
  savePreferred = false;

  // Opciones de Despacho
  shippingMethod = 'DOMICILIO'; // 'DOMICILIO' | 'TIENDA'
  selectedStore = '';
  recommendedStoreNote = '';
  recommendingStore = false;

  tiendasRipley = [
    { name: 'Ripley Jockey Plaza (Lima)', lat: -12.086392, lng: -76.974917, address: 'Av. Javier Prado Este 4200, Surco, Lima' },
    { name: 'Ripley San Miguel (Lima)', lat: -12.077271, lng: -77.085023, address: 'Av. La Marina 2000, San Miguel, Lima' },
    { name: 'Ripley Miraflores (Lima)', lat: -12.121966, lng: -77.029853, address: 'Av. Larco 400, Miraflores, Lima' },
    { name: 'Ripley Plaza Norte (Lima)', lat: -11.993414, lng: -77.062086, address: 'Av. Alfredo Mendiola 1400, Independencia, Lima' },
    { name: 'Ripley Begonias (Lima)', lat: -12.093415, lng: -77.026402, address: 'Calle Las Begonias 500, San Isidro, Lima' },
    { name: 'Ripley Mall Plaza (Trujillo)', lat: -8.098800, lng: -79.039200, address: 'Av. América Oeste s/n, Trujillo, La Libertad' },
    { name: 'Ripley Mall Plaza (Arequipa)', lat: -16.409000, lng: -71.537500, address: 'Av. Ejército 793, Cayma, Arequipa' },
    { name: 'Ripley Mall Aventura (Chiclayo)', lat: -6.771900, lng: -79.838800, address: 'Av. Andrés Avelino Cáceres s/n, Chiclayo, Lambayeque' },
    { name: 'Ripley Plaza del Sol (Piura)', lat: -5.196400, lng: -80.627500, address: 'Av. Grau 1460, Piura' },
    { name: 'Ripley Open Plaza (Huancayo)', lat: -12.067800, lng: -75.210000, address: 'Av. Ferrocarril 146, Huancayo, Junín' }
  ];

  // Combos geográficos
  departamentoSeleccionado = '';
  provinciaSeleccionada = '';
  distritoSeleccionado = '';

  departamentos: string[] = UBIGEO_DATA.map(g => g.departamento);

  get provinciasFiltradas(): string[] {
    if (!this.departamentoSeleccionado) return [];
    const geoData = UBIGEO_DATA.find(g => g.departamento === this.departamentoSeleccionado);
    return geoData ? geoData.provincias.map(p => p.nombre) : [];
  }

  get distritosFiltrados(): string[] {
    if (!this.departamentoSeleccionado || !this.provinciaSeleccionada) return [];
    const geoData = UBIGEO_DATA.find(g => g.departamento === this.departamentoSeleccionado);
    if (!geoData) return [];
    const provData = geoData.provincias.find(p => p.nombre === this.provinciaSeleccionada);
    return provData ? provData.distritos : [];
  }

  // Estados interactivos
  loading = signal(false);
  loadingBrick = signal(false);
  errorMessage = signal<string | null>(null);

  // Cart Signals
  cartItems = this.cartService.cartItems;
  cartSubtotal = this.cartService.cartSubtotal;
  shipping = this.cartService.shipping;
  cartTotal = this.cartService.cartGrandTotal;

  ngOnInit(): void {
    if (this.cartItems().length === 0) {
      this.router.navigate(['/carrito']);
      return;
    }

    const user = this.currentUser();
    if (user) {
      this.firstName = user.firstName || '';
      this.lastName = user.lastName || '';
      this.celular = user.celular || '';
    }

    console.log('✅ Checkout inicializado con Mercado Pago Bricks.');
  }

  // Descuento total de productos
  productsDiscount = computed(() => this.cartService.getTotalDiscount());

  onDepartamentoChange(dept: string): void {
    this.departamentoSeleccionado = dept || '';
    this.provinciaSeleccionada = '';
    this.distritoSeleccionado = '';
  }

  onProvinciaChange(prov: string): void {
    this.provinciaSeleccionada = prov || '';
    this.distritoSeleccionado = '';
  }

  onShippingMethodChange(method: string): void {
    this.shippingMethod = method;
    if (method === 'TIENDA') {
      this.cartService.shipping.set(0);
      this.detectClosestStore();
    } else {
      this.cartService.shipping.set(15);
      this.recommendedStoreNote = '';
    }
  }

  detectClosestStore(): void {
    if (!navigator.geolocation) {
      this.selectedStore = this.tiendasRipley[0].name;
      return;
    }

    this.recommendingStore = true;
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const userLat = position.coords.latitude;
        const userLng = position.coords.longitude;

        let minDistance = Infinity;
        let closestStore = this.tiendasRipley[0];

        this.tiendasRipley.forEach(store => {
          const dist = this.calculateDistance(userLat, userLng, store.lat, store.lng);
          if (dist < minDistance) {
            minDistance = dist;
            closestStore = store;
          }
        });

        this.selectedStore = closestStore.name;
        this.recommendedStoreNote = `Recomendamos ${closestStore.name} por estar a solo ${minDistance.toFixed(1)} km de tu ubicación.`;
        this.recommendingStore = false;
      },
      (error) => {
        console.warn('Error al obtener geolocalización:', error);
        this.selectedStore = this.tiendasRipley[0].name;
        this.recommendingStore = false;
      }
    );
  }

  calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a =
      Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
      Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  }

  // ================================================
  // PASO 1 → PASO 2: Crear orden y mostrar el Brick
  // ================================================
  async onSubmit(): Promise<void> {
    if (this.shippingMethod === 'DOMICILIO') {
      if (!this.streetAddress || !this.streetNumber || !this.departamentoSeleccionado || !this.provinciaSeleccionada || !this.distritoSeleccionado || !this.celular || !this.addressName) {
        this.errorMessage.set('Por favor, completa todos los campos requeridos de despacho.');
        return;
      }
    } else {
      if (!this.celular || !this.selectedStore) {
        this.errorMessage.set('Por favor, ingresa tu celular y selecciona una tienda para el retiro.');
        return;
      }
    }

    const cartItemsList = this.cartItems();
    if (cartItemsList.length === 0) {
      this.errorMessage.set('Tu bolsa de compras está vacía. Agrega productos antes de continuar.');
      return;
    }

    this.errorMessage.set(null);
    this.loading.set(true);

    // Construir dirección de envío
    let fullStreet = '';
    let city = '';
    let state = '';

    if (this.shippingMethod === 'DOMICILIO') {
      fullStreet = this.streetAddress.trim() + ' ' + this.streetNumber.trim();
      if (this.apartment.trim()) fullStreet += ', Depto/Casa: ' + this.apartment.trim();
      if (this.deliveryInstructions.trim()) fullStreet += ' (' + this.deliveryInstructions.trim() + ')';
      city = `${this.provinciaSeleccionada}, ${this.distritoSeleccionado}`;
      state = this.departamentoSeleccionado;
    } else {
      const selectedTienda = this.tiendasRipley.find(t => t.name === this.selectedStore);
      fullStreet = `Retiro en tienda: ${this.selectedStore}. Dirección: ${selectedTienda ? selectedTienda.address : ''}`;
      city = 'Lima';
      state = 'Lima';
    }

    const shippingAddress: AddressRequest = {
      firstName: this.firstName || this.currentUser()?.firstName || 'Cliente',
      lastName: this.lastName || this.currentUser()?.lastName || 'Ripley',
      streetAddress: fullStreet,
      city: city,
      state: state,
      zipCode: this.zipCode || '15000',
      celular: this.celular
    };

    try {
      // 1. Limpiar carrito en backend
      try {
        await lastValueFrom(this.http.put(`${environment.apiUrl}/api/cart/clear`, {}));
      } catch (err) {
        console.warn('[Checkout] No se pudo vaciar el carrito (puede ignorarse):', err);
      }

      // 2. Sincronizar ítems de localStorage al backend de forma secuencial
      for (const item of cartItemsList) {
        await lastValueFrom(
          this.http.post(`${environment.apiUrl}/api/cart/add`, {
            productId: item.product.id,
            size: item.selectedSize,
            color: item.selectedColor,
            quantity: item.quantity
          })
        );
      }

      // 3. Crear la orden en el backend
      const orderPayload = {
        shippingAddress: shippingAddress,
        shippingMethod: this.shippingMethod,
        storeName: this.shippingMethod === 'TIENDA' ? this.selectedStore : '',
        couponCode: this.cartService.couponCode() || null
      };

      this.orderService.createOrder(orderPayload).subscribe({
        next: async (order) => {
          console.log('✅ Orden creada con ID:', order.id);
          this.createdOrderId.set(order.id);
          this.loading.set(false);

          // 4. Solicitar preferencia de pago a Mercado Pago
          await this.initializePaymentBrick(order.id);
        },
        error: (err) => {
          this.loading.set(false);
          console.error('Error al registrar pedido:', err);
          this.errorMessage.set('Hubo un error al guardar tu orden. Inténtalo de nuevo.');
        }
      });
    } catch (err) {
      this.loading.set(false);
      console.error('Error al sincronizar carrito:', err);
      this.errorMessage.set('No se pudo sincronizar tu bolsa de compras. Por favor, inténtalo de nuevo.');
    }
  }

  // ================================================
  // INICIALIZAR EL PAYMENT BRICK DE MERCADO PAGO
  // ================================================
  async initializePaymentBrick(orderId: number): Promise<void> {
    this.loadingBrick.set(true);
    this.errorMessage.set(null);

    try {
      // 4a. Obtener preferenceId desde el backend
      const preference = await lastValueFrom(
        this.orderService.createPaymentPreference(orderId)
      );
      console.log('✅ Preferencia MP creada:', preference.preferenceId);

      // 4b. Avanzar al paso 2 (muestra el contenedor del Brick en el DOM)
      this.checkoutStep.set(2);

      // 4c. Esperar a que Angular renderice el contenedor del Brick
      // Usamos setTimeout con 0ms para ceder el control al ciclo de detección de cambios
      setTimeout(() => {
        this.renderPaymentBrick(preference.preferenceId);
      }, 200);

    } catch (err: any) {
      this.loadingBrick.set(false);
      console.error('[MP] Error al crear preferencia:', err);
      this.errorMessage.set(
        err?.error?.message || 'No se pudo conectar con la pasarela de pago. Por favor, inténtalo de nuevo.'
      );
    }
  }

  // ================================================
  // RENDERIZAR EL PAYMENT BRICK (SDK MP JS v2)
  // ================================================
  private async renderPaymentBrick(preferenceId: string): Promise<void> {
    try {
      // Verificar que el SDK de MP esté disponible (cargado en index.html)
      if (typeof MercadoPago === 'undefined') {
        throw new Error('El SDK de Mercado Pago no está disponible. Verifica tu conexión a internet.');
      }

      // Inicializar el SDK con la Public Key del environment
      const mp = new MercadoPago(environment.mercadoPagoPublicKey, {
        locale: 'es-PE' // Idioma español Perú
      });

      const bricks = mp.bricks();

      // Destruir instancia previa si existe
      if (this.mpBrickController) {
        await this.mpBrickController.unmount();
        this.mpBrickController = null;
      }

      // Configuración del Payment Brick
      const settings = {
        initialization: {
          amount: this.cartTotal(),           // Monto total a cobrar (en soles)
          preferenceId: preferenceId,         // ID de preferencia creada en el backend
        },
        customization: {
          visual: {
            style: {
              theme: 'dark',                  // Tema oscuro para combinar con el diseño
              customVariables: {
                baseColor: '#6e2585',         // Violeta Ripley
                baseColorFirstVariant: '#9b30b0',
                baseColorSecondVariant: '#4a1760',
                fontSizeSmall: '14px',
                fontSizeMedium: '16px',
                formBackgroundColor: '#13131f',
                formInputBackgroundColor: '#1a1a2e',
                formInputTextColor: '#f0f0f5',
                formInputBorderColor: 'rgba(255, 255, 255, 0.15)',
              }
            },
            hideFormTitle: false,
          },
          paymentMethods: {
            // Habilitar tarjeta de crédito, débito y billetera MP
            creditCard: 'all',
            debitCard: 'all',
            mercadoPago: 'all',
            // Excluir métodos no relevantes para el ecommerce
            bankTransfer: ['pix'],            // Solo si quieres incluir transferencias
            ticket: ['bolbradesco', 'pec'],   // Equivalentes a efectivo
            maxInstallments: 12,              // Hasta 12 cuotas
          },
        },
        callbacks: {
          onReady: () => {
            // El Brick está listo y renderizado en pantalla
            this.ngZone.run(() => {
              this.loadingBrick.set(false);
              console.log('✅ Payment Brick listo y renderizado.');
            });
          },
          onSubmit: ({ selectedPaymentMethod, formData }: any) => {
            // El usuario hizo clic en "Pagar" dentro del Brick
            console.log('💳 Usuario confirmó pago. Método:', selectedPaymentMethod);
            return new Promise<void>((resolve, reject) => {
              const orderId = this.createdOrderId();
              if (orderId) {
                this.orderService.processPayment(orderId, formData).subscribe({
                  next: (response) => {
                    console.log('✅ Pago procesado correctamente', response);
                    this.ngZone.run(() => {
                      this.router.navigate(['/checkout/success'], { queryParams: { orderId: orderId } });
                    });
                    resolve();
                  },
                  error: (error) => {
                    console.error('❌ Error procesando el pago', error);
                    this.ngZone.run(() => {
                      this.errorMessage.set('Error al procesar el pago. Inténtalo de nuevo.');
                    });
                    reject();
                  }
                });
              } else {
                reject();
              }
            });
          },
          onError: (error: any) => {
            this.ngZone.run(() => {
              console.error('[MP Brick] Error:', error);
              if (error.type !== 'non_critical') {
                this.errorMessage.set('Ocurrió un error en el formulario de pago. Por favor, recarga la página.');
              }
            });
          }
        }
      };

      // Renderizar el Brick en el contenedor HTML con id="mp-payment-brick-container"
      this.mpBrickController = await bricks.create(
        'payment',
        'mp-payment-brick-container',
        settings
      );

    } catch (err: any) {
      this.ngZone.run(() => {
        this.loadingBrick.set(false);
        console.error('[MP] Error al renderizar Payment Brick:', err);
        this.errorMessage.set(
          err?.message || 'No se pudo renderizar el formulario de pago. Por favor, inténtalo de nuevo.'
        );
      });
    }
  }

  // ================================================
  // HELPERS DE VISUALIZACIÓN DEL CARRITO
  // ================================================
  getItemPrice(item: CartItem): number {
    const p = item.product as any;
    return p.precio !== undefined ? p.precio : (p.price !== undefined ? p.price : 0);
  }

  getItemTitle(item: CartItem): string {
    const p = item.product as any;
    return p.nombre || p.title || 'Producto';
  }

  getItemImage(item: CartItem): string {
    const p = item.product as any;
    if (p.colores && Array.isArray(p.colores)) {
      const colorObj = p.colores.find((c: any) => c.nombre === item.selectedColor);
      if (colorObj && colorObj.imagenes && colorObj.imagenes.length > 0) {
        return colorObj.imagenes[0];
      }
    }
    if (p.imagenes && p.imagenes.length > 0) return p.imagenes[0];
    if (p.mainImageUrl) return p.mainImageUrl;
    return 'assets/placeholder.png';
  }
}
