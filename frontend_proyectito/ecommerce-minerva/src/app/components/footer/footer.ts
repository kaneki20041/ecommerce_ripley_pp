import { Component } from '@angular/core';

import { RouterLink } from '@angular/router';

interface FooterLink {
  label: string;
  route: string;
}

interface FooterColumn {
  title: string;
  links: FooterLink[];
}

interface SocialLink {
  name: string;
  icon: string;
  url: string;
}

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './footer.html',
  styleUrls: ['./footer.scss'],
})
export class FooterComponent {
  currentYear = new Date().getFullYear();

  footerColumns: FooterColumn[] = [
    {
      title: 'Categorías',
      links: [
        { label: 'Uniformes Escolares', route: '/uniformes' },
        { label: 'Buzos Deportivos', route: '/deportivos' },
        { label: 'Calzado', route: '/calzado' },
        { label: 'Casual', route: '/casual' },
        { label: 'Accesorios', route: '/accesorios' },
      ],
    },
    {
      title: 'Ayuda',
      links: [
        { label: 'Preguntas Frecuentes', route: '/faq' },
        { label: 'Guía de Tallas', route: '/guia-tallas' },
        { label: 'Envíos y Devoluciones', route: '/envios' },
        { label: 'Rastrear Pedido', route: '/rastrear' },
        { label: 'Contacto', route: '/contacto' },
      ],
    },
    {
      title: 'Empresa',
      links: [
        { label: 'Nosotros', route: '/nosotros' },
        { label: 'Tiendas', route: '/tiendas' },
        { label: 'Trabaja con Nosotros', route: '/trabaja' },
        { label: 'Términos y Condiciones', route: '/terminos' },
        { label: 'Política de Privacidad', route: '/privacidad' },
      ],
    },
  ];

  socialLinks: SocialLink[] = [
    {
      name: 'Facebook',
      icon: 'facebook',
      url: 'https://facebook.com',
    },
    {
      name: 'Instagram',
      icon: 'instagram',
      url: 'https://instagram.com',
    },
    {
      name: 'Twitter',
      icon: 'twitter',
      url: 'https://twitter.com',
    },
    {
      name: 'YouTube',
      icon: 'youtube',
      url: 'https://youtube.com',
    },
  ];

  paymentMethods = [
    { name: 'Visa', icon: 'visa' },
    { name: 'Mastercard', icon: 'mastercard' },
    { name: 'Yape', icon: 'yape' },
    { name: 'Plin', icon: 'plin' },
  ];

  scrollToTop(): void {
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }
}
