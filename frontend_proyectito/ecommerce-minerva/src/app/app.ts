import { Component, inject } from '@angular/core';

import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './components/navbar/navbar';
import { FooterComponent } from './components/footer/footer';
import { AuthModalComponent } from './components/auth-modal/auth-modal';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent, FooterComponent, AuthModalComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.scss'],
})
export class App {
  title = 'tienda-textil';
  private router = inject(Router);
  showLayout = true;

  constructor() {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        const url = event.urlAfterRedirects;
        this.showLayout = !url.includes('/admin') && !url.includes('/employee') && !url.includes('/cuenta');
      }
    });
  }
}
