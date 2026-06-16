import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { CategoryService, CategoryResponse, CategoryFlatResponse, CategoryRequest } from '../../../services/category.service';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardSidebarComponent],
  templateUrl: './categories.html',
  styleUrls: ['./categories.scss']
})
export class CategoriesComponent implements OnInit {
  private categoryService = inject(CategoryService);

  // States
  categoriesSignal = signal<CategoryResponse[]>([]);
  flatCategoriesSignal = signal<CategoryFlatResponse[]>([]);
  loading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  // Tree Expansion State
  expandedIds = new Set<number>();

  // Modals state
  showAddEditModal = false;
  isEditMode = false;
  selectedCategoryId: number | null = null;

  // Form Fields
  formName = '';
  formPadreId: number | null = null;
  formActivo = true;
  formLevel = 1;

  // Dynamic filter for parent selection
  availableParents: CategoryFlatResponse[] = [];

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    // Cargar árbol de categorías jerárquico
    this.categoryService.getAdminCategoryTree().subscribe({
      next: (res) => {
        if (res.result && res.data) {
          this.categoriesSignal.set(res.data);
          
          // Por defecto expandir las categorías principales (Nivel 1) al inicio
          res.data.forEach(cat => this.expandedIds.add(cat.id));
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set('Error al cargar la jerarquía de categorías');
        this.loading.set(false);
      }
    });

    // Cargar lista plana para selectores
    this.categoryService.getAdminFlatCategories().subscribe({
      next: (res) => {
        if (res.result && res.data) {
          this.flatCategoriesSignal.set(res.data);
        }
      }
    });
  }

  // --- Tree Controls ---
  isExpanded(id: number): boolean {
    return this.expandedIds.has(id);
  }

  toggleNode(id: number, event: MouseEvent): void {
    event.stopPropagation();
    if (this.expandedIds.has(id)) {
      this.expandedIds.delete(id);
    } else {
      this.expandedIds.add(id);
    }
  }

  expandAll(): void {
    const traverse = (cats: CategoryResponse[]) => {
      cats.forEach(c => {
        this.expandedIds.add(c.id);
        if (c.subcategories && c.subcategories.length > 0) {
          traverse(c.subcategories);
        }
      });
    };
    traverse(this.categoriesSignal());
  }

  collapseAll(): void {
    this.expandedIds.clear();
  }

  // --- Modal Forms ---
  openAddModal(parent?: CategoryResponse): void {
    this.isEditMode = false;
    this.selectedCategoryId = null;
    this.formName = '';
    this.formActivo = true;
    
    if (parent) {
      this.formPadreId = parent.id;
      this.formLevel = parent.level + 1;
    } else {
      this.formPadreId = null;
      this.formLevel = 1;
    }

    this.filterAvailableParents();
    this.showAddEditModal = true;
  }

  openEditModal(category: CategoryResponse): void {
    this.isEditMode = true;
    this.selectedCategoryId = category.id;
    this.formName = category.name;
    this.formPadreId = category.padreId || null;
    this.formLevel = category.level;
    this.formActivo = category.activo;

    this.filterAvailableParents();
    this.showAddEditModal = true;
  }

  closeModal(): void {
    this.showAddEditModal = false;
    this.errorMessage.set(null);
  }

  filterAvailableParents(): void {
    const list = this.flatCategoriesSignal();
    
    // Filtrar la lista de categorías que pueden ser padres:
    // 1. No pueden estar en Nivel 3 (las categorías de nivel 3 no pueden tener hijos, ya que el límite es nivel 3).
    // 2. Si estamos editando, una categoría no puede ser su propio padre ni descendiente.
    this.availableParents = list.filter(cat => {
      if (cat.level >= 3) return false;
      if (this.isEditMode && cat.id === this.selectedCategoryId) return false;
      
      // Si estamos editando, también evitar subcategorías descendientes como padres
      if (this.isEditMode && this.selectedCategoryId) {
        let parentId: number | null = cat.padreId || null;
        while (parentId !== null) {
          if (parentId === this.selectedCategoryId) return false;
          // Buscar el abuelo
          const grandParent = list.find(c => c.id === parentId);
          parentId = grandParent ? (grandParent.padreId || null) : null;
        }
      }
      return true;
    });
  }

  onParentChange(): void {
    if (this.formPadreId) {
      const parent = this.flatCategoriesSignal().find(c => c.id === this.formPadreId);
      this.formLevel = parent ? parent.level + 1 : 1;
    } else {
      this.formLevel = 1;
    }
  }

  submitForm(): void {
    if (!this.formName || !this.formName.trim()) {
      this.errorMessage.set('El nombre de la categoría es obligatorio.');
      return;
    }

    if (this.formLevel > 3) {
      this.errorMessage.set('No se permiten niveles de categorías superiores a 3.');
      return;
    }

    this.loading.set(true);
    const req: CategoryRequest = {
      name: this.formName.trim(),
      padreCategoriaId: this.formPadreId,
      level: this.formLevel,
      activo: this.formActivo
    };

    if (this.isEditMode && this.selectedCategoryId) {
      // Editar
      this.categoryService.updateCategory(this.selectedCategoryId, req).subscribe({
        next: (res) => {
          if (res.result) {
            this.showSuccess('Categoría actualizada con éxito.');
            this.loadCategories();
            this.closeModal();
          } else {
            this.errorMessage.set(res.message || 'Error al actualizar la categoría.');
            this.loading.set(false);
          }
        },
        error: (err) => {
          this.errorMessage.set(err.error?.message || 'Error en el servidor al actualizar la categoría.');
          this.loading.set(false);
        }
      });
    } else {
      // Crear
      this.categoryService.createCategory(req).subscribe({
        next: (res) => {
          if (res.result) {
            this.showSuccess('Categoría creada con éxito.');
            this.loadCategories();
            this.closeModal();
          } else {
            this.errorMessage.set(res.message || 'Error al crear la categoría.');
            this.loading.set(false);
          }
        },
        error: (err) => {
          this.errorMessage.set(err.error?.message || 'Error en el servidor al crear la categoría.');
          this.loading.set(false);
        }
      });
    }
  }

  // --- Soft Delete / Toggle Active Status ---
  toggleStatus(category: CategoryResponse, event: MouseEvent): void {
    event.stopPropagation();
    this.loading.set(true);

    this.categoryService.toggleCategoryStatus(category.id).subscribe({
      next: (res) => {
        if (res.result) {
          const action = res.data?.activo ? 'activada' : 'desactivada (soft-delete)';
          this.showSuccess(`Categoría "${category.name}" ${action} con éxito.`);
          this.loadCategories();
        } else {
          this.errorMessage.set(res.message || 'Error al cambiar el estado de la categoría.');
          this.loading.set(false);
        }
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Error de conexión al cambiar el estado.');
        this.loading.set(false);
      }
    });
  }

  showSuccess(msg: string): void {
    this.successMessage.set(msg);
    setTimeout(() => this.successMessage.set(null), 4000);
  }
}
