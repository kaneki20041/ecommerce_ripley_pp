import { Injectable, signal } from '@angular/core';

export type AuthModalView = 'login' | 'register' | 'forgot-password';

@Injectable({
  providedIn: 'root'
})
export class AuthModalService {
  isOpen = signal<boolean>(false);
  currentView = signal<AuthModalView>('login');

  open(view: AuthModalView = 'login') {
    this.currentView.set(view);
    this.isOpen.set(true);
    // Prevenir scroll del body cuando el modal está abierto
    document.body.style.overflow = 'hidden';
  }

  close() {
    this.isOpen.set(false);
    // Restaurar scroll del body
    document.body.style.overflow = 'auto';
  }

  switchView(view: AuthModalView) {
    this.currentView.set(view);
  }
}