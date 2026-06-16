import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { DataTableComponent, TableColumn, TableAction } from '../../../components/data-table/data-table';
import { ModalComponent } from '../../../components/modal/modal';
import { UserService } from '../../../services/user.service';
import { AdminUsuarioResponse, AdminCreateUserRequest, AdminUpdateUserRequest } from '../../../models/user.model';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardSidebarComponent, DataTableComponent, ModalComponent],
  templateUrl: './users.html',
  styleUrls: ['./users.scss']
})
export class UsersComponent implements OnInit {
  private userService = inject(UserService);

  loading = signal(false);
  showCreateModal = signal(false);
  showEditModal = signal(false);

  // Form data
  userForm = {
    id: 0,
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    role: 'USER' as 'ADMIN' | 'EMPLOYEE' | 'USER',
    celular: ''
  };

  // Filters
  roleFilter = signal<string>('all');
  statusFilter = signal<string>('all');
  pageSize = signal<number>(10);

  // Table config
  columns: TableColumn[] = [
    { key: 'id', label: 'ID', sortable: true, width: '80px' },
    { key: 'fullName', label: 'Usuario', sortable: true },
    { key: 'email', label: 'Email', sortable: true },
    { key: 'role', label: 'Rol', type: 'badge', sortable: true },
    { key: 'status', label: 'Estado', type: 'badge', sortable: true },
    { key: 'createdAt', label: 'Fecha Registro', type: 'date', sortable: true }
  ];

  usersData = signal<AdminUsuarioResponse[]>([]);

  // Computed signals for reactive counters
  totalUsers = computed(() => this.usersData().length);
  totalAdmin = computed(() => this.usersData().filter(u => u.role === 'ADMIN').length);
  totalEmployee = computed(() => this.usersData().filter(u => u.role === 'EMPLOYEE').length);
  totalUser = computed(() => this.usersData().filter(u => u.role === 'USER').length);

  actions: TableAction[] = [
    {
      label: 'Editar',
      icon: 'edit',
      color: '#2196F3',
      onClick: (row: any) => this.editUser(row.original)
    },
    {
      label: 'Eliminar',
      icon: 'delete',
      color: '#FF5252',
      onClick: (row: any) => this.deleteUser(row.original)
    },
    {
      label: 'Desactivar',
      icon: 'x',
      color: '#FF9800',
      onClick: (row: any) => this.toggleStatus(row.original),
      show: (row: any) => row.status === 'activo'
    },
    {
      label: 'Activar',
      icon: 'check',
      color: '#4CAF50',
      onClick: (row: any) => this.toggleStatus(row.original),
      show: (row: any) => row.status === 'inactivo'
    }
  ];

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.userService.getAllUsers().subscribe({
      next: (response) => {
        if (response.result && response.data) {
          this.usersData.set(response.data);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error al cargar usuarios:', error);
        this.loading.set(false);
      }
    });
  }

  openCreateModal(): void {
    this.resetForm();
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
    this.resetForm();
  }

  createUser(): void {
    const req: AdminCreateUserRequest = {
      firstName: this.userForm.firstName,
      lastName: this.userForm.lastName,
      email: this.userForm.email,
      password: this.userForm.password,
      role: this.userForm.role,
      celular: this.userForm.celular || undefined
    };

    this.userService.createUserByAdmin(req).subscribe({
      next: (response) => {
        if (response.result) {
          this.loadUsers(); // Refresh the list
          this.closeCreateModal();
        }
      },
      error: (error) => {
        console.error('Error al crear usuario:', error);
      }
    });
  }

  editUser(user: AdminUsuarioResponse): void {
    this.userForm = {
      id: user.id,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      password: '', // Leave blank, optional
      role: user.role as 'ADMIN' | 'EMPLOYEE' | 'USER',
      celular: ''
    };
    this.showEditModal.set(true);
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
    this.resetForm();
  }

  updateUser(): void {
    if (!this.userForm.id) return;

    const req: AdminUpdateUserRequest = {
      firstName: this.userForm.firstName,
      lastName: this.userForm.lastName,
      email: this.userForm.email,
      role: this.userForm.role,
      password: this.userForm.password ? this.userForm.password : undefined
    };

    this.userService.updateUserByAdmin(this.userForm.id, req).subscribe({
      next: (response) => {
        if (response.result) {
          this.loadUsers();
          this.closeEditModal();
        }
      },
      error: (error) => {
        console.error('Error al actualizar usuario:', error);
      }
    });
  }

  deleteUser(user: AdminUsuarioResponse): void {
    if (confirm(`¿Estás seguro de eliminar a ${user.firstName} ${user.lastName}?`)) {
      this.userService.deleteUser(user.id).subscribe({
        next: (response) => {
          if (response.result) {
            this.loadUsers();
          }
        },
        error: (error) => {
          console.error('Error al eliminar usuario:', error);
        }
      });
    }
  }

  toggleStatus(user: AdminUsuarioResponse): void {
    this.userService.toggleUserStatus(user.id).subscribe({
      next: (response) => {
        if (response.result) {
          this.loadUsers();
        }
      },
      error: (error) => {
        console.error('Error al cambiar estado del usuario:', error);
      }
    });
  }

  resetForm(): void {
    this.userForm = {
      id: 0,
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      role: 'USER',
      celular: ''
    };
  }

  // Computed signal to map the data for the data-table component
  filteredUsers = computed(() => {
    let filtered = this.usersData();

    if (this.roleFilter() !== 'all') {
      filtered = filtered.filter(u => u.role === this.roleFilter());
    }

    if (this.statusFilter() !== 'all') {
      const activeFilter = this.statusFilter() === 'activo';
      filtered = filtered.filter(u => u.active === activeFilter);
    }

    // Map to the format the table expects
    return filtered.map(u => ({
      ...u,
      fullName: `${u.firstName} ${u.lastName}`,
      status: u.active ? 'activo' : 'inactivo',
      original: u // Keep original data for actions
    }));
  });
}