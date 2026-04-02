import { Component, OnInit, OnDestroy } from '@angular/core';

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
  imports: [],
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
      title: 'Nueva Colección Escolar 2026',
      subtitle: 'Uniformes de alta calidad para el nuevo año escolar',
      ctaText: 'Ver Colección',
      ctaLink: '/uniformes',
    },
    {
      id: 2,
      image: 'assets/images/carousel/slide2.jpg',
      title: 'Deportes y Confort',
      subtitle: 'Buzos deportivos para todas las actividades',
      ctaText: 'Comprar Ahora',
      ctaLink: '/deportivos',
    },
    {
      id: 3,
      image: 'assets/images/carousel/slide3.jpg',
      title: 'Estilo Casual',
      subtitle: 'Moda cómoda para el día a día',
      ctaText: 'Explorar',
      ctaLink: '/casual',
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
