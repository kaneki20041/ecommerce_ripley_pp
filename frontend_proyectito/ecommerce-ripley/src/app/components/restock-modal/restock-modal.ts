import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface RestockItem {
  variantId: number;
  sku: string;
  name: string;
  stock: number;
  category: string;
  price: number;
}

@Component({
  selector: 'app-restock-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './restock-modal.html',
  styleUrls: ['./restock-modal.scss']
})
export class RestockModalComponent implements OnChanges {
  @Input() item: RestockItem | null = null;
  @Input() visible = false;

  @Output() closed = new EventEmitter<void>();
  @Output() confirmed = new EventEmitter<{ variantId: number; cantidad: number }>();

  cantidad = 1;
  errorMsg = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible) {
      this.cantidad = 1;
      this.errorMsg = '';
    }
  }

  close(): void {
    this.closed.emit();
  }

  confirm(): void {
    if (!this.cantidad || this.cantidad < 1) {
      this.errorMsg = 'La cantidad debe ser al menos 1.';
      return;
    }
    if (!Number.isInteger(this.cantidad)) {
      this.errorMsg = 'La cantidad debe ser un número entero.';
      return;
    }
    this.errorMsg = '';
    this.confirmed.emit({ variantId: this.item!.variantId, cantidad: this.cantidad });
  }

  get stockAfterRestock(): number {
    return (this.item?.stock ?? 0) + (this.cantidad ?? 0);
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.close();
    }
  }
}
