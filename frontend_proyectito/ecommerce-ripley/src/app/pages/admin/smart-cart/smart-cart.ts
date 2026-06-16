import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardSidebarComponent } from '../../../components/dashboard-sidebar/dashboard-sidebar';
import { SmartCartService, SmartCartConfig } from '../../../services/smart-cart.service';
import { ToastService } from '../../../services/toast.service';

interface AiMetricOption {
  key: string;
  label: string;
  icon: string;
  desc: string;
}

@Component({
  selector: 'app-smart-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardSidebarComponent],
  templateUrl: './smart-cart.html',
  styleUrls: ['./smart-cart.scss']
})
export class SmartCartAdminComponent implements OnInit {
  private smartCartService = inject(SmartCartService);
  private toastService = inject(ToastService);

  loading = signal(true);
  saving = signal(false);

  config: SmartCartConfig = {
    active: true,
    upsellDiscountPercentage: 10,
    maxProductsToShow: 4,
    recommendationCriteria: 'MOST_SOLD',
    minStockRequired: 5,
    aiMetrics: 'USER_HISTORY,CTR_DATA'
  };

  // Array de métricas activas para los checkboxes
  activeAiMetrics: string[] = ['USER_HISTORY', 'CTR_DATA'];

  /** Opciones del checklist de datos del prompt de Groq */
  readonly aiMetricOptions: AiMetricOption[] = [
    {
      key: 'USER_HISTORY',
      label: 'Historial de Compras',
      icon: '👤',
      desc: 'Envía el género, categorías y marcas preferidas del cliente a la IA.'
    },
    {
      key: 'CTR_DATA',
      label: 'Popularidad (CTR)',
      icon: '🔥',
      desc: 'Envía la tasa de clics de cada candidato para que la IA priorice los más populares.'
    },
    {
      key: 'NEW_ARRIVALS',
      label: 'Nuevos Ingresos',
      icon: '🆕',
      desc: 'Indica a la IA qué productos son nuevos para que los destaque en sus sugerencias.'
    },
    {
      key: 'DISCOUNTS',
      label: 'Ofertas y Descuentos',
      icon: '🏷️',
      desc: 'Envía el porcentaje de descuento y le pide a la IA considerar los productos en liquidación.'
    },
    {
      key: 'PRICE_MATCH',
      label: 'Balance de Precios',
      icon: '💰',
      desc: 'Envía el precio de cada candidato para que la IA sugiera complementos coherentes al presupuesto.'
    }
  ];

  get isAiSelected(): boolean {
    return this.config.recommendationCriteria === 'AI_RECOMMENDED';
  }

  ngOnInit(): void {
    this.loadConfig();
  }

  loadConfig(): void {
    this.loading.set(true);
    this.smartCartService.getConfig().subscribe({
      next: (response) => {
        if (response.result && response.data) {
          this.config = { ...response.data };
          // Parsear el CSV de aiMetrics en un array
          this.activeAiMetrics = this.config.aiMetrics
            ? this.config.aiMetrics.split(',').map(m => m.trim()).filter(m => m.length > 0)
            : ['USER_HISTORY', 'CTR_DATA'];
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading config:', error);
        this.toastService.error('Error', 'No se pudo cargar la configuración.');
        this.loading.set(false);
      }
    });
  }

  isMetricActive(key: string): boolean {
    return this.activeAiMetrics.includes(key);
  }

  toggleMetric(key: string): void {
    const index = this.activeAiMetrics.indexOf(key);
    if (index >= 0) {
      // No permitir desmarcar si solo queda 1 activa
      if (this.activeAiMetrics.length > 1) {
        this.activeAiMetrics.splice(index, 1);
      } else {
        this.toastService.error('Atención', 'Debe haber al menos una métrica activa para el prompt de la IA.');
      }
    } else {
      this.activeAiMetrics.push(key);
    }
  }

  getMetricLabel(key: string): string {
    return this.aiMetricOptions.find(o => o.key === key)?.label ?? key;
  }

  saveConfig(): void {
    // Sincronizar el array de métricas al string CSV antes de guardar
    this.config.aiMetrics = this.activeAiMetrics.join(',');

    this.saving.set(true);
    this.smartCartService.updateConfig(this.config).subscribe({
      next: (response) => {
        if (response.result) {
          this.config = { ...response.data };
          this.activeAiMetrics = this.config.aiMetrics
            ? this.config.aiMetrics.split(',').map(m => m.trim()).filter(m => m.length > 0)
            : ['USER_HISTORY', 'CTR_DATA'];
          this.toastService.success('Carrito Inteligente', 'Configuración guardada exitosamente.');
        } else {
          this.toastService.error('Error', response.message || 'Error al guardar la configuración.');
        }
        this.saving.set(false);
      },
      error: (error) => {
        console.error('Error saving config:', error);
        this.toastService.error('Error', 'Error de conexión al guardar la configuración.');
        this.saving.set(false);
      }
    });
  }
}
