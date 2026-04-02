import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { AuthService } from '../../../services/auth';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardSidebarComponent],
  templateUrl: './profile.html',
  styleUrls: ['./profile.scss']
})
export class ProfileComponent {
  private authService = inject(AuthService);
  currentUser = this.authService.currentUser;
  userInitials = computed(() => {
    const user = this.currentUser();
    return user ? `${user.firstName[0]}${user.lastName[0]}` : '';
  });

  profileForm: any = {
    firstName: this.currentUser()?.firstName || '',
    lastName: this.currentUser()?.lastName || '',
    email: this.currentUser()?.email || '',
    celular: this.currentUser()?.celular || '',
    direccion: this.currentUser()?.direccion || '',
    fechaNacimiento: this.currentUser()?.fechaNacimiento || '',
    foto: this.currentUser()?.foto || ''
  };

  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  activeTab = 'info';

  constructor() { }

  saveGeneralInfo(): void {
    console.log('Guardar información general requerida:', {
      foto: this.profileForm.foto,
      fechaNacimiento: this.profileForm.fechaNacimiento,
      direccion: this.profileForm.direccion
    });
  }

  changePassword(): void {
    if(this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      alert("Las contraseñas nuevas no coinciden");
      return;
    }
    console.log('Cambiar contraseña ejecutado', this.passwordForm);
    // Reiniciar form
    this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
  }
}