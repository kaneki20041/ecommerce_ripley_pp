import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal.html',
  styleUrls: ['./modal.scss']
})
export class ModalComponent implements OnInit, OnDestroy {
  @Input() title: string = '';
  @Input() size: 'sm' | 'md' | 'lg' | 'xl' = 'md';
  @Input() showFooter: boolean = true;
  @Input() closeOnBackdrop: boolean = true;
  @Input() closeOnEscape: boolean = true;

  @Output() onClose = new EventEmitter<void>();
  @Output() onConfirm = new EventEmitter<void>();

  ngOnInit(): void {
    // Prevenir scroll del body
    document.body.style.overflow = 'hidden';

    // Listener para ESC key
    if (this.closeOnEscape) {
      document.addEventListener('keydown', this.handleEscape);
    }
  }

  ngOnDestroy(): void {
    // Restaurar scroll del body
    document.body.style.overflow = '';

    // Remover listener
    document.removeEventListener('keydown', this.handleEscape);
  }

  handleEscape = (event: KeyboardEvent): void => {
    if (event.key === 'Escape') {
      this.close();
    }
  };

  close(): void {
    this.onClose.emit();
  }

  confirm(): void {
    this.onConfirm.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (this.closeOnBackdrop && event.target === event.currentTarget) {
      this.close();
    }
  }
}