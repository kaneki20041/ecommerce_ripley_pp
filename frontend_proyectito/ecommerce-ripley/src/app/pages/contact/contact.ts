import { Component } from '@angular/core';

import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MyFileValidators } from '../../shared/validators/file-validators';

interface ContactInfo {
  icon: string;
  title: string;
  value: string;
  link?: string;
}

interface SocialMedia {
  name: string;
  icon: string;
  url: string;
  color: string;
}

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './contact.html',
  styleUrls: ['./contact.scss'],
})
export class ContactComponent {
  contactForm: FormGroup;
  isSubmitting = false;
  submitSuccess = false;
  submitError = false;

  contactInfo: ContactInfo[] = [
    {
      icon: 'phone',
      title: 'Teléfono',
      value: '+51 944 123 456',
      link: 'tel:+51944123456',
    },
    {
      icon: 'email',
      title: 'Email',
      value: 'contacto@tiendatextil.com',
      link: 'mailto:contacto@tiendatextil.com',
    },
    {
      icon: 'location',
      title: 'Dirección',
      value: 'Av. Principal 123, Trujillo, La Libertad, Perú',
    },
    {
      icon: 'clock',
      title: 'Horario de Atención',
      value: 'Lun - Sáb: 9:00 AM - 7:00 PM',
    },
  ];

  socialMedia: SocialMedia[] = [
    {
      name: 'Facebook',
      icon: 'facebook',
      url: 'https://facebook.com/tiendatextil',
      color: '#1877F2',
    },
    {
      name: 'Instagram',
      icon: 'instagram',
      url: 'https://instagram.com/tiendatextil',
      color: '#E4405F',
    },
    {
      name: 'WhatsApp',
      icon: 'whatsapp',
      url: 'https://wa.me/51944123456',
      color: '#25D366',
    },
    {
      name: 'TikTok',
      icon: 'tiktok',
      url: 'https://tiktok.com/@tiendatextil',
      color: '#000000',
    },
  ];

  constructor(private fb: FormBuilder) {
    this.contactForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.pattern(/^\d{9}$/)]],
      asunto: ['', [Validators.required]],
      mensaje: ['', [Validators.required, Validators.minLength(20)]],
      archivos_adjuntos: [
        null,
        [
          MyFileValidators.maxSize(5 * 1024 * 1024), // 5MB
          MyFileValidators.extensions(['pdf', 'jpg', 'png']),
        ],
      ],
    });
  }

  onSubmit() {
    if (this.contactForm.invalid) {
      this.markFormGroupTouched(this.contactForm);
      return;
    }

    this.isSubmitting = true;
    this.submitError = false;
    this.submitSuccess = false;

    // Simular envío (aquí conectarás con tu backend)
    setTimeout(() => {
      this.isSubmitting = false;
      this.submitSuccess = true;
      this.contactForm.reset();

      // Ocultar mensaje de éxito después de 5 segundos
      setTimeout(() => {
        this.submitSuccess = false;
      }, 5000);
    }, 2000);

    // TODO: Implementar llamada real al backend
    // this.contactService.sendMessage(this.contactForm.value).subscribe({
    //   next: () => {
    //     this.isSubmitting = false;
    //     this.submitSuccess = true;
    //     this.contactForm.reset();
    //   },
    //   error: () => {
    //     this.isSubmitting = false;
    //     this.submitError = true;
    //   }
    // });
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach((key) => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  onFileChange(event: any) {
    const files = event.target.files;
    if (files && files.length > 0) {
      this.contactForm.patchValue({
        archivos_adjuntos: files,
      });
      this.contactForm.get('archivos_adjuntos')?.markAsTouched();
      this.contactForm.get('archivos_adjuntos')?.updateValueAndValidity();
    } else {
      this.contactForm.patchValue({
        archivos_adjuntos: null,
      });
      this.contactForm.get('archivos_adjuntos')?.markAsTouched();
      this.contactForm.get('archivos_adjuntos')?.updateValueAndValidity();
    }
  }

  get archivoError() {
    const control = this.contactForm.get('archivos_adjuntos');
    if (control?.hasError('maxSizeExceeded')) return 'El archivo es muy pesado (Máx 5MB)';
    if (control?.hasError('invalidExtension')) return 'Formato no permitido (Usa PDF, JPG o PNG)';
    return null;
  }
  // Getters para validación
  get nombre() {
    return this.contactForm.get('nombre');
  }
  get email() {
    return this.contactForm.get('email');
  }
  get telefono() {
    return this.contactForm.get('telefono');
  }
  get asunto() {
    return this.contactForm.get('asunto');
  }
  get mensaje() {
    return this.contactForm.get('mensaje');
  }
  get archivos_adjuntos() {
    return this.contactForm.get('archivos_adjuntos');
  }
}
