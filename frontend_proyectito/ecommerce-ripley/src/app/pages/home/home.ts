import {
  Component, OnInit, OnDestroy, HostListener,
  ElementRef, ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CarouselComponent } from '../../components/carousel/carousel';
import { FeaturedProductsComponent } from '../../components/featured-products/featured-products';

interface Category {
  id: number;
  name: string;
  description: string;
  badge: string;
  image: string;
  link: string;
  color: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, CarouselComponent, FeaturedProductsComponent],
  templateUrl: './home.html',
  styleUrls: ['./home.scss']
})
export class HomeComponent implements OnInit, OnDestroy {

  // ── Cursor personalizado ──────────────────────────────────────────────────
  cursorX = 0;
  cursorY = 0;
  cursorVisible = false;

  // ── Tarjetas de Categorías con imagen real ───────────────────────────────
  categories: Category[] = [
    {
      id: 1,
      name: 'Moda Mujer',
      description: 'Las últimas tendencias para ella',
      badge: 'Nueva Colección',
      image: 'assets/images/categories/moda-mujer.jpg',
      link: '/productos',
      color: '#61005A'
    },
    {
      id: 2,
      name: 'Moda Hombre',
      description: 'Estilo y elegancia para él',
      badge: 'Temporada 2025',
      image: 'assets/images/categories/moda-hombre.jpg',
      link: '/productos',
      color: '#3D0037'
    },
    {
      id: 3,
      name: 'Deportes',
      description: 'Rendimiento sin límites',
      badge: 'Top Marcas',
      image: 'assets/images/categories/deportes.jpg',
      link: '/productos',
      color: '#8E155A'
    }
  ];

  cardRotations: { rotateX: number; rotateY: number }[] = this.categories.map(() => ({ rotateX: 0, rotateY: 0 }));

  // ── Contadores animados ───────────────────────────────────────────────────
  stats = [
    { label: 'Productos',        target: 5000,   current: 0, suffix: '+' },
    { label: 'Clientes Felices', target: 120000, current: 0, suffix: '+' },
    { label: 'Años en el Mercado', target: 75,   current: 0, suffix: ''  },
    { label: 'Tiendas en Perú',  target: 35,     current: 0, suffix: '+' }
  ];
  private statsAnimated = false;
  private animationIntervals: ReturnType<typeof setInterval>[] = [];

  @ViewChild('statsSection') statsSection!: ElementRef;

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.animationIntervals.forEach(clearInterval);
  }

  @HostListener('document:mousemove', ['$event'])
  onMouseMove(e: MouseEvent): void {
    this.cursorX = e.clientX;
    this.cursorY = e.clientY;
    this.cursorVisible = true;
  }

  onCardMouseMove(e: MouseEvent, index: number): void {
    const card = e.currentTarget as HTMLElement;
    const rect  = card.getBoundingClientRect();
    const rotateY  = ((e.clientX - rect.left - rect.width  / 2) / (rect.width  / 2)) * 12;
    const rotateX  = -((e.clientY - rect.top  - rect.height / 2) / (rect.height / 2)) * 12;
    this.cardRotations[index] = { rotateX, rotateY };
  }

  onCardMouseLeave(index: number): void {
    this.cardRotations[index] = { rotateX: 0, rotateY: 0 };
  }

  getCardStyle(index: number): { [key: string]: string } {
    const r = this.cardRotations[index];
    const isResting = r.rotateX === 0 && r.rotateY === 0;
    return {
      transform: `perspective(800px) rotateX(${r.rotateX}deg) rotateY(${r.rotateY}deg) scale3d(${isResting ? 1 : 1.02},${isResting ? 1 : 1.02},1)`,
      transition: isResting
        ? 'transform 0.6s cubic-bezier(0.23, 1, 0.32, 1)'
        : 'transform 0.08s linear'
    };
  }

  @HostListener('window:scroll')
  onScroll(): void {
    if (this.statsAnimated || !this.statsSection) return;
    const rect = this.statsSection.nativeElement.getBoundingClientRect();
    if (rect.top < window.innerHeight - 80) {
      this.statsAnimated = true;
      this.animateCounters();
    }
  }

  private animateCounters(): void {
    this.stats.forEach((stat, i) => {
      const steps = 60;
      const increment = stat.target / steps;
      let current = 0;
      const interval = setInterval(() => {
        current += increment;
        if (current >= stat.target) {
          this.stats[i].current = stat.target;
          clearInterval(interval);
        } else {
          this.stats[i].current = Math.floor(current);
        }
      }, 2000 / steps);
      this.animationIntervals.push(interval);
    });
  }
}
