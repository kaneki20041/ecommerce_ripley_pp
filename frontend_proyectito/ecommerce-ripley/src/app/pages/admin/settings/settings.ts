import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [FormsModule, DashboardSidebarComponent],
  templateUrl: './settings.html',
  styleUrls: ['./settings.scss']
})
export class SettingsComponent {
  activeTab = signal('general');

  generalSettings = {
    siteName: 'Tienda Textil',
    email: 'contacto@tiendatextil.com',
    phone: '+51 999 888 777',
    address: 'Av. Larco 1234, Miraflores, Lima'
  };

  shippingSettings = {
    freeShippingThreshold: 150,
    standardShippingCost: 15,
    expressShippingCost: 25
  };

  saveSettings(): void {
    console.log('Guardar configuración');
  }
}