import { Component, computed, effect } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { AuthService} from '../../services/auth';
import { RegisterRequest } from '../../models/auth.model';
import { AuthModalService } from '../../services/auth-modal';

@Component({
  selector: 'app-auth-modal',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './auth-modal.html',
  styleUrls: ['./auth-modal.scss'],
})
export class AuthModalComponent {
  loginForm: FormGroup;
  registerForm: FormGroup;
  forgotPasswordForm: FormGroup;

  isLoading = false;
  errorMessage = '';
  successMessage = '';
  passwordVisible = false;
  confirmPasswordVisible = false;

  // Computed values
  isOpen = computed(() => this.authModalService.isOpen());
  currentView = computed(() => this.authModalService.currentView());

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    public authModalService: AuthModalService,
  ) {
    // Inicializar formularios
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false],
    });

    this.registerForm = this.fb.group(
      {
        nombre: ['', [Validators.required, Validators.minLength(2)]],
        apellido: ['', [Validators.required, Validators.minLength(2)]],
        email: ['', [Validators.required, Validators.email]],
        telefono: ['', [Validators.pattern(/^\d{9}$/)]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validators: this.passwordMatchValidator },
    );

    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });

    // Resetear mensajes cuando cambia la vista
    effect(() => {
      this.currentView();
      this.clearMessages();
    });
  }

  // Validador personalizado para confirmar contraseña
  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

onLogin() {
    if (this.loginForm.invalid) {
      this.markFormGroupTouched(this.loginForm);
      return;
    }

    this.isLoading = true;
    this.clearMessages();

    const { email, password } = this.loginForm.value;

    this.authService
      .login({ email, password })
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (response) => {
          this.successMessage = '¡Inicio de sesión exitoso!';
          setTimeout(() => {
            this.authModalService.close();
            this.loginForm.reset();
          }, 800);
        },
        error: (error) => {
          this.errorMessage =
            error.error?.message || 'Error al iniciar sesión. Verifica tus credenciales.';
        },
      });
  }

  onRegister() {
    if (this.registerForm.invalid) {
      this.markFormGroupTouched(this.registerForm);
      return;
    }

    this.isLoading = true;
    this.clearMessages();

    // 1. Extraemos los datos del formulario (en español)
    const formValues = this.registerForm.value;

    // 2. Los "traducimos" al formato del Backend (inglés/spanglish)
    const userData: RegisterRequest = {
      firstName: formValues.nombre,
      lastName: formValues.apellido,
      email: formValues.email,
      celular: formValues.telefono,
      password: formValues.password
    };

    // 3. Enviamos la data traducida
    this.authService
      .register(userData)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (response) => {
          setTimeout(() => {
            this.authModalService.close();
            this.registerForm.reset();
          }, 1000);
        },
        error: (error) => {
          // error.error?.message intentará leer el mensaje de tu AuthResponse del backend
          this.errorMessage = error.error?.message || 'Error al registrarse. Intenta nuevamente.';
        },
      });
  }

  onForgotPassword() {
    if (this.forgotPasswordForm.invalid) {
      this.markFormGroupTouched(this.forgotPasswordForm);
      return;
    }

    this.isLoading = true;
    this.clearMessages();

    const { email } = this.forgotPasswordForm.value;

    // TODO: Descomentar cuando el endpoint de reset de contraseña esté disponible.
    // this.authService
    //   .requestPasswordReset(email)
    //   .pipe(finalize(() => (this.isLoading = false)))
    //   .subscribe({
    //     next: (response) => {
    //       this.successMessage =
    //         'Se ha enviado un correo con instrucciones para restablecer tu contraseña.';
    //       setTimeout(() => {
    //         this.authModalService.switchView('login');
    //         this.forgotPasswordForm.reset();
    //       }, 3000);
    //     },
    //     error: (error) => {
    //       this.errorMessage = error.error?.message || 'Error al enviar correo. Intenta nuevamente.';
    //     },
    //   });
  }

  closeModal() {
    this.authModalService.close();
    this.clearMessages();
    this.resetForms();
  }

  switchView(view: 'login' | 'register' | 'forgot-password') {
    this.authModalService.switchView(view);
  }

  togglePasswordVisibility(field: 'password' | 'confirmPassword') {
    if (field === 'password') {
      this.passwordVisible = !this.passwordVisible;
    } else {
      this.confirmPasswordVisible = !this.confirmPasswordVisible;
    }
  }

  private clearMessages() {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private resetForms() {
    this.loginForm.reset();
    this.registerForm.reset();
    this.forgotPasswordForm.reset();
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach((key) => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  // Getters para validación
  get loginEmail() {
    return this.loginForm.get('email');
  }
  get loginPassword() {
    return this.loginForm.get('password');
  }

  get registerNombre() {
    return this.registerForm.get('nombre');
  }
  get registerApellido() {
    return this.registerForm.get('apellido');
  }
  get registerEmail() {
    return this.registerForm.get('email');
  }
  get registerTelefono() {
    return this.registerForm.get('telefono');
  }
  get registerPassword() {
    return this.registerForm.get('password');
  }
  get registerConfirmPassword() {
    return this.registerForm.get('confirmPassword');
  }

  get forgotEmail() {
    return this.forgotPasswordForm.get('email');
  }
}
