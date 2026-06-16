import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';

interface CarouselSlide {
  id: number;
  image: string;
  title: string;
  subtitle: string;
  ctaText: string;
  ctaLink: string;
}

@Component({
  selector: 'app-carousel',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './carousel.html',
  styleUrls: ['./carousel.scss'],
})
export class CarouselComponent implements OnInit, OnDestroy {
  currentSlide = 0;
  autoPlayInterval: any;
  isTransitioning = false;

  slides: CarouselSlide[] = [
    {
      id: 1,
      image: 'assets/images/carousel/slide1.jpg',
      title: 'Nueva Temporada\nPrimavera 2025',
      subtitle: 'Descubre las tendencias que marcan la moda este año',
      ctaText: 'Ver Colección',
      ctaLink: '/productos',
    },
    {
      id: 2,
      image: 'assets/images/carousel/slide2.jpg',
      title: 'Alto Rendimiento,\nEstilo Supremo',
      subtitle: 'Equipamiento deportivo premium para tus mejores momentos',
      ctaText: 'Comprar Ahora',
      ctaLink: '/productos',
    },
    {
      id: 3,
      image: 'assets/images/carousel/slide3.jpg',
      title: 'Tu Hogar,\nTu Estilo de Vida',
      subtitle: 'Tecnología y diseño para transformar cada espacio',
      ctaText: 'Explorar Todo',
      ctaLink: '/productos',
    },
  ];

  ngOnInit() {
    this.startAutoPlay();
  }

  ngOnDestroy() {
    this.stopAutoPlay();
  }

  startAutoPlay() {
    this.autoPlayInterval = setInterval(() => {
      this.nextSlide();
    }, 5000);
  }

  stopAutoPlay() {
    if (this.autoPlayInterval) {
      clearInterval(this.autoPlayInterval);
    }
  }

  nextSlide() {
    if (this.isTransitioning) return;

    this.isTransitioning = true;
    this.currentSlide = (this.currentSlide + 1) % this.slides.length;

    setTimeout(() => {
      this.isTransitioning = false;
    }, 500);
  }

  prevSlide() {
    if (this.isTransitioning) return;

    this.isTransitioning = true;
    this.currentSlide = this.currentSlide === 0 ? this.slides.length - 1 : this.currentSlide - 1;

    setTimeout(() => {
      this.isTransitioning = false;
    }, 500);
  }

  goToSlide(index: number) {
    if (this.isTransitioning || index === this.currentSlide) return;

    this.isTransitioning = true;
    this.currentSlide = index;
    this.stopAutoPlay();
    this.startAutoPlay();

    setTimeout(() => {
      this.isTransitioning = false;
    }, 500);
  }

  onMouseEnter() {
    this.stopAutoPlay();
  }

  onMouseLeave() {
    this.startAutoPlay();
  }
}
