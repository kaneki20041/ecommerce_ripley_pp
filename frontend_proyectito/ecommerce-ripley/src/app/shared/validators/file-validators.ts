import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export class MyFileValidators {

    // Validación de peso máximo
    static maxSize(maxSizeInBytes: number): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const files = control.value as FileList;
            if (files && files.length > 0) {
                for (let i = 0; i < files.length; i++) {
                    if (files[i].size > maxSizeInBytes) {
                        return { 'maxSizeExceeded': { actual: files[i].size, max: maxSizeInBytes } };
                    }
                }
            }
            return null;
        };
    }

    // Validación de extensiones
    static extensions(allowedExtensions: string[]): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const files = control.value as FileList;
            if (files && files.length > 0) {
                for (let i = 0; i < files.length; i++) {
                    const extension = files[i].name.split('.').pop()?.toLowerCase();
                    if (extension && !allowedExtensions.includes(extension)) {
                        return { 'invalidExtension': { allowed: allowedExtensions, actual: extension } };
                    }
                }
            }
            return null;
        };
    }
}