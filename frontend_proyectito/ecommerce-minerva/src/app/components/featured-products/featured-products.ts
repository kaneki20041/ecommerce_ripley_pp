import { Component } from '@angular/core';

interface Product {
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
  imports: [],
  templateUrl: './featured-products.html',
  styleUrls: ['./featured-products.scss'],
})
export class FeaturedProductsComponent {
  products: Product[] = [
    {
      id: 1,
      name: 'Camisa Escolar Blanca',
      category: 'Uniformes Escolares',
      price: 45.9,
      oldPrice: 59.9,
      image: 'assets/images/products/camisa-escolar.jpg',
      rating: 4.8,
      reviews: 124,
      discount: 23,
    },
    {
      id: 2,
      name: 'Buzo Deportivo Azul',
      category: 'Buzos Escolares',
      price: 89.9,
      image: 'assets/images/products/buzo-deportivo.jpg',
      rating: 4.9,
      reviews: 98,
      isNew: true,
    },
    {
      id: 3,
      name: 'Zapatillas Running',
      category: 'Calzado',
      price: 149.9,
      oldPrice: 199.9,
      image: 'assets/images/products/zapatillas.jpg',
      rating: 4.7,
      reviews: 156,
      discount: 25,
    },
    {
      id: 4,
      name: 'Polo Cuello Cerrado',
      category: 'Casual',
      price: 35.9,
      image: 'assets/images/products/polo-casual.jpg',
      rating: 4.6,
      reviews: 87,
      isNew: true,
    },
    {
      id: 5,
      name: 'Pantalón Escolar',
      category: 'Uniformes Escolares',
      price: 55.9,
      oldPrice: 69.9,
      image: 'assets/images/products/pantalon-escolar.jpg',
      rating: 4.8,
      reviews: 142,
      discount: 20,
    },
    {
      id: 6,
      name: 'Casaca Deportiva',
      category: 'Buzos Escolares',
      price: 119.9,
      image: 'assets/images/products/casaca-deportiva.jpg',
      rating: 4.9,
      reviews: 201,
      isNew: true,
    },
    {
      id: 7,
      name: 'Corbata Escolar',
      category: 'Accesorios',
      price: 19.9,
      image: 'assets/images/products/corbata.jpg',
      rating: 4.5,
      reviews: 76,
    },
    {
      id: 8,
      name: 'Pullover Azul',
      category: 'Uniformes Escolares',
      price: 69.9,
      oldPrice: 89.9,
      image: 'assets/images/products/pullover.jpg',
      rating: 4.7,
      reviews: 113,
      discount: 22,
    },
  ];

  addToCart(product: Product) {
    console.log('Agregado al carrito:', product);
    // Aquí implementarás la lógica del carrito
  }

  generateStars(rating: number): number[] {
    return Array(5)
      .fill(0)
      .map((_, i) => (i < Math.round(rating) ? 1 : 0));
  }
}
