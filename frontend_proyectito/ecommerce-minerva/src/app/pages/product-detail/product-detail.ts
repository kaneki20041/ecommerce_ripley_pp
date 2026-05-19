import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { Product, ProductCardResponse, mapProductToCardResponse } from '../../models/product.model';
import { ProductCardComponent } from '../../components/product-card/product-card';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, ProductCardComponent, RouterModule],
  templateUrl: './product-detail.html',
  styleUrls: ['./product-detail.scss']
})
export class ProductDetailComponent implements OnInit {
  product = signal<Product | undefined>(undefined);
  relatedProducts = signal<ProductCardResponse[]>([]);
  loading = signal(true);

  // Imagen seleccionada
  selectedImageIndex = signal(0);

  // Selección de variantes
  selectedSize = signal<string>('');
  selectedColor = signal<string>('');

  // Cantidad
  quantity = signal(1);

  // Estado de agregar al carrito
  addingToCart = signal(false);
  addedToCart = signal(false);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const id = +params['id'];
      this.loadProduct(id);
    });
  }

  loadProduct(id: number): void {
    this.loading.set(true);

    this.productService.getProductById(id).subscribe({
      next: (product) => {
        if (product) {
          this.product.set(product);

          // Seleccionar primera talla y color por defecto
          this.selectedSize.set(product.tallas[0]);
          this.selectedColor.set(product.colores[0].nombre);

          // Cargar productos relacionados
          this.loadRelatedProducts(id);
        } else {
          this.router.navigate(['/productos']);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error cargando producto:', error);
        this.loading.set(false);
        this.router.navigate(['/productos']);
      }
    });
  }

  loadRelatedProducts(productId: number): void {
    this.productService.getRelatedProducts(productId, 4).subscribe({
      next: (products) => {
        this.relatedProducts.set(products.map(p => mapProductToCardResponse(p)));
      }
    });
  }

  // Galería de imágenes
  selectImage(index: number): void {
    this.selectedImageIndex.set(index);
  }

  nextImage(): void {
    const product = this.product();
    if (!product) return;

    const current = this.selectedImageIndex();
    const next = current + 1 >= product.imagenes.length ? 0 : current + 1;
    this.selectedImageIndex.set(next);
  }

  previousImage(): void {
    const product = this.product();
    if (!product) return;

    const current = this.selectedImageIndex();
    const prev = current - 1 < 0 ? product.imagenes.length - 1 : current - 1;
    this.selectedImageIndex.set(prev);
  }

  // Selección de variantes
  selectSize(size: string): void {
    this.selectedSize.set(size);
  }

  selectColor(color: string): void {
    this.selectedColor.set(color);
  }

  getSelectedColorHex(): string {
    const product = this.product();
    if (!product) return '#000000';

    const color = product.colores.find(c => c.nombre === this.selectedColor());
    return color?.hex || '#000000';
  }

  // Cantidad
  increaseQuantity(): void {
    const product = this.product();
    if (!product) return;

    if (this.quantity() < product.stock) {
      this.quantity.set(this.quantity() + 1);
    }
  }

  decreaseQuantity(): void {
    if (this.quantity() > 1) {
      this.quantity.set(this.quantity() - 1);
    }
  }

  // Carrito
  addToCart(): void {
    const product = this.product();
    if (!product) return;

    this.addingToCart.set(true);

    // Simular delay
    setTimeout(() => {
      this.cartService.addToCart(
        product,
        this.quantity(),
        this.selectedSize(),
        this.selectedColor()
      );

      this.addingToCart.set(false);
      this.addedToCart.set(true);

      // Reset después de 2 segundos
      setTimeout(() => {
        this.addedToCart.set(false);
      }, 2000);
    }, 500);
  }

  buyNow(): void {
    this.addToCart();

    setTimeout(() => {
      this.router.navigate(['/carrito']);
    }, 600);
  }

  // Helpers
  getRatingStars(): number[] {
    return Array(5).fill(0).map((_, i) => i);
  }

  getDiscountPercentage(): number {
    const product = this.product();
    if (!product || !product.descuento) return 0;
    return Math.round(product.descuento);
  }

  getDiscount(): number {
    const product = this.product();
    if (!product || !product.precioAnterior) return 0;
    return product.precioAnterior - product.precio;
  }

  // Navegación
  goBack(): void {
    this.router.navigate(['/productos']);
  }

  goToCart(): void {
    this.router.navigate(['/carrito']);
  }
}