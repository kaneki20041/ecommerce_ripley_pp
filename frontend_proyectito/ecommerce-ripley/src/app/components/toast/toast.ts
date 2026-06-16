import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast toast--{{ toast.type }}" (click)="toastService.dismiss(toast.id)">
          <div class="toast__icon">
            @if (toast.type === 'success') {
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            } @else if (toast.type === 'error') {
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            } @else if (toast.type === 'warning') {
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            } @else {
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            }
          </div>
          <div class="toast__body">
            <p class="toast__title">{{ toast.title }}</p>
            @if (toast.message) {
              <p class="toast__msg">{{ toast.message }}</p>
            }
          </div>
          <button class="toast__close" (click)="toastService.dismiss(toast.id)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 1.5rem;
      right: 1.5rem;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      max-width: 380px;
      width: 100%;
    }
    .toast {
      display: flex;
      align-items: flex-start;
      gap: 0.875rem;
      padding: 1rem 1rem 1rem 1.125rem;
      border-radius: 12px;
      background: #fff;
      box-shadow: 0 8px 32px rgba(0,0,0,0.12), 0 2px 8px rgba(0,0,0,0.08);
      border-left: 4px solid;
      animation: slideIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
      cursor: pointer;
      font-family: 'Inter', sans-serif;
    }
    @keyframes slideIn {
      from { transform: translateX(110%); opacity: 0; }
      to   { transform: translateX(0);   opacity: 1; }
    }
    .toast--success { border-color: #22c55e; }
    .toast--success .toast__icon { color: #22c55e; background: #f0fdf4; }
    .toast--error   { border-color: #ef4444; }
    .toast--error   .toast__icon { color: #ef4444; background: #fef2f2; }
    .toast--warning { border-color: #f59e0b; }
    .toast--warning .toast__icon { color: #f59e0b; background: #fffbeb; }
    .toast--info    { border-color: #3b82f6; }
    .toast--info    .toast__icon { color: #3b82f6; background: #eff6ff; }

    .toast__icon {
      width: 36px; height: 36px;
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      flex-shrink: 0;
    }
    .toast__icon svg { width: 18px; height: 18px; }
    .toast__body { flex: 1; min-width: 0; }
    .toast__title {
      margin: 0;
      font-size: 0.9rem;
      font-weight: 700;
      color: #111;
      line-height: 1.3;
    }
    .toast__msg {
      margin: 0.25rem 0 0;
      font-size: 0.82rem;
      color: #555;
      line-height: 1.4;
    }
    .toast__close {
      background: none; border: none; cursor: pointer;
      color: #aaa; padding: 0; flex-shrink: 0;
      transition: color 0.15s;
      display: flex; align-items: center;
    }
    .toast__close:hover { color: #333; }
    .toast__close svg { width: 16px; height: 16px; }
  `]
})
export class ToastComponent {
  toastService = inject(ToastService);
}
