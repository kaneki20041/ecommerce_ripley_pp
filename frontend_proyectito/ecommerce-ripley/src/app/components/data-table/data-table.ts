import { Component, Input, Output, EventEmitter, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface TableColumn {
  key: string;
  label: string;
  sortable?: boolean;
  type?: 'text' | 'number' | 'date' | 'badge' | 'image' | 'custom' | 'stockAlert';
  width?: string;
}

export interface TableAction {
  label: string;
  icon: string;
  color: string;
  onClick: (row: any) => void;
  show?: (row: any) => boolean;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './data-table.html',
  styleUrls: ['./data-table.scss']
})
export class DataTableComponent {
  @Input() columns: TableColumn[] = [];
  @Input() set data(val: any[]) {
    this._data.set(val || []);
  }
  get data(): any[] {
    return this._data();
  }
  private _data = signal<any[]>([]);

  @Input() actions: TableAction[] = [];
  @Input() loading: boolean = false;
  @Input() selectable: boolean = false;
  @Input() pagination: boolean = true;
  @Input() itemsPerPage: number = 10;
  @Input() emptyMessage: string = 'No hay datos disponibles';
  @Input() serverSidePagination: boolean = false;
  @Input() totalItems: number = 0;

  @Input() pageSizeOptions: number[] = [10, 15, 20];

  @Input() set page(val: number) {
    this.currentPage.set(val);
  }

  @Output() pageChange = new EventEmitter<number>();
  @Output() pageSizeChange = new EventEmitter<number>();
  @Output() onSort = new EventEmitter<{ column: string; direction: 'asc' | 'desc' }>();
  @Output() onSelect = new EventEmitter<any[]>();

  // Signals
  searchTerm = signal('');
  sortColumn = signal<string>('');
  sortDirection = signal<'asc' | 'desc'>('asc');
  currentPage = signal(1);
  selectedRows = signal<Set<any>>(new Set());

  // Computed
  filteredData = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const currentData = this._data();

    if (!term) return currentData;

    return currentData.filter(row => {
      return this.columns.some(col => {
        const value = this.getNestedValue(row, col.key);
        return String(value).toLowerCase().includes(term);
      });
    });
  });

  sortedData = computed(() => {
    const data = [...this.filteredData()];
    const column = this.sortColumn();
    const direction = this.sortDirection();

    if (!column) return data;

    return data.sort((a, b) => {
      const aVal = this.getNestedValue(a, column);
      const bVal = this.getNestedValue(b, column);

      if (aVal < bVal) return direction === 'asc' ? -1 : 1;
      if (aVal > bVal) return direction === 'asc' ? 1 : -1;
      return 0;
    });
  });

  paginatedData = computed(() => {
    if (!this.pagination) return this.sortedData();

    // Si la paginación viene del servidor, ya nos mandaron solo 10 elementos.
    // No debemos cortarlos de nuevo.
    if (this.serverSidePagination) {
      return this.sortedData();
    }

    // Paginación local normal (lo que ya tenías)
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.sortedData().slice(start, end);
  });

  totalPages = computed(() => {
    if (this.serverSidePagination) {
      // Usa el total que le mandó Spring Boot
      return Math.ceil(this.totalItems / this.itemsPerPage);
    }
    // Usa el total de la lista local
    return Math.ceil(this.sortedData().length / this.itemsPerPage);
  });

  allSelected = computed(() => {
    return this.paginatedData().length > 0 &&
      this.paginatedData().every(row => this.selectedRows().has(row));
  });

  // Methods
  sort(column: TableColumn): void {
    if (!column.sortable) return;

    if (this.sortColumn() === column.key) {
      this.sortDirection.set(this.sortDirection() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortColumn.set(column.key);
      this.sortDirection.set('asc');
    }

    this.onSort.emit({ column: column.key, direction: this.sortDirection() });
  }

  toggleSelectAll(): void {
    const selected = new Set(this.selectedRows());

    if (this.allSelected()) {
      this.paginatedData().forEach(row => selected.delete(row));
    } else {
      this.paginatedData().forEach(row => selected.add(row));
    }

    this.selectedRows.set(selected);
    this.onSelect.emit(Array.from(selected));
  }

  toggleSelect(row: any): void {
    const selected = new Set(this.selectedRows());

    if (selected.has(row)) {
      selected.delete(row);
    } else {
      selected.add(row);
    }

    this.selectedRows.set(selected);
    this.onSelect.emit(Array.from(selected));
  }

  isSelected(row: any): boolean {
    return this.selectedRows().has(row);
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages()) return;

    this.currentPage.set(page);

    if (this.serverSidePagination) {
      // Si es modo servidor, avisamos al padre (restamos 1 porque Spring Boot cuenta desde 0)
      this.pageChange.emit(page - 1);
    }
  }

  onPageSizeChange(newSize: number): void {
    this.itemsPerPage = newSize;
    this.currentPage.set(1);
    this.pageSizeChange.emit(newSize);
    
    if (this.serverSidePagination) {
      this.pageChange.emit(0);
    }
  }

  getNestedValue(obj: any, path: string): any {
    return path.split('.').reduce((prev, curr) => prev?.[curr], obj);
  }

  formatValue(row: any, column: TableColumn): string {
    const value = this.getNestedValue(row, column.key);

    if (column.type === 'date' && value) {
      return new Date(value).toLocaleDateString('es-PE');
    }

    if (column.type === 'number' && value !== null && value !== undefined) {
      return value.toLocaleString('es-PE');
    }

    return value || '-';
  }

  shouldShowAction(action: TableAction, row: any): boolean {
    return action.show ? action.show(row) : true;
  }

  getPaginationRange(): number[] {
    const total = this.totalPages();
    const current = this.currentPage();
    const range: number[] = [];

    if (total <= 7) {
      for (let i = 1; i <= total; i++) {
        range.push(i);
      }
    } else {
      if (current <= 3) {
        range.push(1, 2, 3, 4, -1, total);
      } else if (current >= total - 2) {
        range.push(1, -1, total - 3, total - 2, total - 1, total);
      } else {
        range.push(1, -1, current - 1, current, current + 1, -1, total);
      }
    }

    return range;
  }
}