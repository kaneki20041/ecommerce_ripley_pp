import { Component } from '@angular/core';

import { CarouselComponent } from '../../components/carousel/carousel';
import { FeaturedProductsComponent } from '../../components/featured-products/featured-products';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CarouselComponent, FeaturedProductsComponent],
  template: `
    <div class="home-page">
      <app-carousel></app-carousel>
      <app-featured-products></app-featured-products>
    </div>
  `,
  styles: [
    `
      .home-page {
        width: 100%;
      }
    `,
  ],
})
export class HomeComponent {}
