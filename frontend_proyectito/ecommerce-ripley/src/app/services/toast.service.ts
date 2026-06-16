import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts = signal<Toast[]>([]);
  private nextId = 0;

  show(type: Toast['type'], title: string, message?: string, duration = 4000): void {
    const id = ++this.nextId;
    this.toasts.update(t => [...t, { id, type, title, message }]);
    setTimeout(() => this.dismiss(id), duration);
  }

  success(title: string, message?: string): void { this.show('success', title, message); }
  error(title: string, message?: string): void    { this.show('error',   title, message, 5000); }
  warning(title: string, message?: string): void  { this.show('warning', title, message); }
  info(title: string, message?: string): void     { this.show('info',    title, message); }

  dismiss(id: number): void {
    this.toasts.update(t => t.filter(toast => toast.id !== id));
  }
}
