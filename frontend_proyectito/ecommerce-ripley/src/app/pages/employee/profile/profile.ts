import { Component, computed, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { AuthService } from '../../../services/auth';

@Component({
  selector: 'app-employee-profile',
  standalone: true,
  imports: [FormsModule, DashboardSidebarComponent],
  templateUrl: './profile.html',
  styleUrls: ['./profile.scss']
})
export class EmployeeProfileComponent {
  private authService = inject(AuthService);

  currentUser = this.authService.currentUser;
  userInitials = computed(() => {
    const user = this.currentUser();
    return user ? `${user.firstName[0]}${user.lastName[0]}` : '';
  });

  activeTab = 'info';

  profileForm = {
    firstName: this.currentUser()?.firstName || '',
    lastName: this.currentUser()?.lastName || '',
    email: this.currentUser()?.email || '',
    phone: '',
    department: 'Ventas'
  };

  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  constructor() { }

  saveProfile(): void {
    console.log('Guardar perfil:', this.profileForm);
  }

  changePassword(): void {
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      alert('Las contraseñas no coinciden');
      return;
    }
    console.log('Cambiar contraseña');
  }
}