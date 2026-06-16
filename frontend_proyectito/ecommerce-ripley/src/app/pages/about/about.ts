import { Component } from '@angular/core';

interface TimelineItem {
  year: string;
  title: string;
  description: string;
}

interface ValueItem {
  icon: string;
  title: string;
  description: string;
}

interface TeamMember {
  name: string;
  position: string;
  image: string;
  bio: string;
}

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [],
  templateUrl: './about.html',
  styleUrls: ['./about.scss'],
})
export class AboutComponent {
  timeline: TimelineItem[] = [
    {
      year: '2008',
      title: 'Inicio de la empresa',
      description:
        'Comenzamos como una pequeña tienda familiar en Trujillo, con el sueño de vestir a las familias peruanas con calidad y estilo.',
    },
    {
      year: '2012',
      title: 'Expansión regional',
      description:
        'Abrimos nuestra segunda tienda y comenzamos a distribuir a colegios de toda la región La Libertad.',
    },
    {
      year: '2018',
      title: 'Transformación digital',
      description:
        'Lanzamos nuestra plataforma e-commerce, llevando nuestros productos a todo el Perú.',
    },
    {
      year: '2024',
      title: 'Líderes en textiles escolares',
      description:
        'Nos consolidamos como una de las principales empresas de prendas textiles escolares y deportivas del norte del país.',
    },
  ];

  values: ValueItem[] = [
    {
      icon: 'quality',
      title: 'Calidad Garantizada',
      description:
        'Todos nuestros productos pasan por rigurosos controles de calidad para asegurar durabilidad y confort.',
    },
    {
      icon: 'price',
      title: 'Precios Justos',
      description:
        'Trabajamos directamente con fabricantes para ofrecer los mejores precios sin intermediarios.',
    },
    {
      icon: 'service',
      title: 'Atención Personalizada',
      description:
        'Nuestro equipo está capacitado para asesorarte y encontrar exactamente lo que necesitas.',
    },
    {
      icon: 'sustainable',
      title: 'Compromiso Social',
      description:
        'Apoyamos a comunidades locales y trabajamos con proveedores que respetan el medio ambiente.',
    },
  ];

  team: TeamMember[] = [
    {
      name: 'María Gonzales',
      position: 'Fundadora & CEO',
      image: 'assets/images/team/maria.jpg',
      bio: 'Con más de 15 años de experiencia en el sector textil.',
    },
    {
      name: 'Carlos Rodríguez',
      position: 'Director de Operaciones',
      image: 'assets/images/team/carlos.jpg',
      bio: 'Especialista en logística y cadena de suministro.',
    },
    {
      name: 'Ana Torres',
      position: 'Gerente de Ventas',
      image: 'assets/images/team/ana.jpg',
      bio: 'Experta en atención al cliente y desarrollo comercial.',
    },
  ];

  stats = [
    { number: '15+', label: 'Años de experiencia' },
    { number: '50K+', label: 'Clientes satisfechos' },
    { number: '200+', label: 'Colegios asociados' },
    { number: '99%', label: 'Satisfacción del cliente' },
  ];
}
