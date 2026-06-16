export interface Product {
    id: number;
    nombre: string;
    descripcion: string;
    precio: number;
    precioAnterior?: number;
    descuento?: number;
    categoria: string;
    subcategoria?: string;
    genero: 'Mujer' | 'Hombre' | 'Unisex';
    imagenes: string[];
    tallas: string[];
    colores: Color[];
    stock: number;
    nuevo: boolean;
    destacado: boolean;
    rating: number;
    reviews: number;
    marca?: string;
    material?: string;
    cuidados?: string[];
    variantes?: any[];
}

export interface Color {
    id?: number;
    nombre: string;
    hexCode: string;
    imagenes?: string[];
}

export interface Talla {
    id?: number;
    valor: string;
    tipo: string;
}

export const MOCK_PRODUCTS: Product[] = [
    // UNIFORMES ESCOLARES - MUJER
    {
        id: 1,
        nombre: 'Camisa Escolar Blanca Mujer',
        descripcion: 'Camisa escolar de algodón premium, perfecta para el uso diario. Tela suave y respirable que mantiene la frescura todo el día.',
        precio: 45.90,
        precioAnterior: 59.90,
        descuento: 23,
        categoria: 'Uniformes Escolares',
        subcategoria: 'Camisas',
        genero: 'Mujer',
        imagenes: [
            'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=500',
            'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=500',
            'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=500'
        ],
        tallas: ['6', '8', '10', '12', '14', '16'],
        colores: [
            { nombre: 'Blanco', hexCode: '#FFFFFF' }
        ],
        stock: 50,
        nuevo: false,
        destacado: true,
        rating: 4.5,
        reviews: 28,
        marca: 'TextilPro',
        material: '65% Algodón, 35% Poliéster',
        cuidados: ['Lavar a máquina agua fría', 'No usar blanqueador', 'Planchar temperatura media']
    },
    {
        id: 2,
        nombre: 'Falda Escolar Azul Marino',
        descripcion: 'Falda tableada escolar en azul marino, confeccionada con tela de alta calidad que mantiene su forma lavado tras lavado.',
        precio: 52.90,
        categoria: 'Uniformes Escolares',
        subcategoria: 'Faldas',
        genero: 'Mujer',
        imagenes: [
            'https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500',
            'https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=500'
        ],
        tallas: ['6', '8', '10', '12', '14', '16'],
        colores: [
            { nombre: 'Azul Marino', hexCode: '#000080' }
        ],
        stock: 35,
        nuevo: false,
        destacado: true,
        rating: 4.8,
        reviews: 42,
        marca: 'TextilPro',
        material: '70% Poliéster, 30% Viscosa'
    },

    // UNIFORMES ESCOLARES - HOMBRE
    {
        id: 3,
        nombre: 'Camisa Escolar Blanca Hombre',
        descripcion: 'Camisa escolar masculina de manga larga, elaborada en algodón de primera calidad. Diseño clásico y duradero.',
        precio: 48.90,
        precioAnterior: 62.90,
        descuento: 22,
        categoria: 'Uniformes Escolares',
        subcategoria: 'Camisas',
        genero: 'Hombre',
        imagenes: [
            'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500',
            'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500'
        ],
        tallas: ['6', '8', '10', '12', '14', '16'],
        colores: [
            { nombre: 'Blanco', hexCode: '#FFFFFF' }
        ],
        stock: 45,
        nuevo: false,
        destacado: true,
        rating: 4.6,
        reviews: 35,
        marca: 'TextilPro',
        material: '65% Algodón, 35% Poliéster'
    },
    {
        id: 4,
        nombre: 'Pantalón Escolar Gris',
        descripcion: 'Pantalón escolar en tela gabardina gris, resistente y cómodo. Diseño clásico con bolsillos laterales.',
        precio: 65.90,
        categoria: 'Uniformes Escolares',
        subcategoria: 'Pantalones',
        genero: 'Hombre',
        imagenes: [
            'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=500',
            'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=500'
        ],
        tallas: ['6', '8', '10', '12', '14', '16'],
        colores: [
            { nombre: 'Gris', hexCode: '#808080' }
        ],
        stock: 40,
        nuevo: false,
        destacado: false,
        rating: 4.4,
        reviews: 22,
        marca: 'TextilPro',
        material: '65% Poliéster, 35% Viscosa'
    },

    // BUZOS ESCOLARES
    {
        id: 5,
        nombre: 'Buzo Escolar con Cierre',
        descripcion: 'Buzo deportivo escolar con cierre frontal y bolsillos. Perfecto para educación física y uso casual.',
        precio: 78.90,
        categoria: 'Buzos Escolares',
        genero: 'Unisex',
        imagenes: [
            'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500',
            'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500'
        ],
        tallas: ['S', 'M', 'L', 'XL'],
        colores: [
            { nombre: 'Azul Marino', hexCode: '#000080' },
            { nombre: 'Gris', hexCode: '#808080' }
        ],
        stock: 60,
        nuevo: true,
        destacado: true,
        rating: 4.7,
        reviews: 18,
        marca: 'SportSchool',
        material: '80% Algodón, 20% Poliéster'
    },
    {
        id: 6,
        nombre: 'Polera Deportiva Escolar',
        descripcion: 'Polera deportiva de secado rápido, ideal para actividades físicas. Tecnología antibacterial.',
        precio: 42.90,
        categoria: 'Buzos Escolares',
        genero: 'Unisex',
        imagenes: [
            'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500'
        ],
        tallas: ['S', 'M', 'L', 'XL'],
        colores: [
            { nombre: 'Blanco', hexCode: '#FFFFFF' },
            { nombre: 'Azul', hexCode: '#0000FF' }
        ],
        stock: 70,
        nuevo: true,
        destacado: false,
        rating: 4.5,
        reviews: 24,
        marca: 'SportSchool',
        material: '100% Poliéster'
    },

    // CALZADO
    {
        id: 7,
        nombre: 'Zapatos Escolares Negros',
        descripcion: 'Zapatos escolares de cuero sintético, suela antideslizante y plantilla acolchada para mayor comodidad.',
        precio: 89.90,
        precioAnterior: 119.90,
        descuento: 25,
        categoria: 'Calzado',
        genero: 'Unisex',
        imagenes: [
            'https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=500',
            'https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=500'
        ],
        tallas: ['32', '33', '34', '35', '36', '37', '38', '39', '40'],
        colores: [
            { nombre: 'Negro', hexCode: '#000000' }
        ],
        stock: 25,
        nuevo: false,
        destacado: true,
        rating: 4.6,
        reviews: 51,
        marca: 'SchoolFeet',
        material: 'Cuero sintético'
    },
    {
        id: 8,
        nombre: 'Zapatillas Deportivas Escolares',
        descripcion: 'Zapatillas deportivas con suela de goma y diseño ergonómico. Perfectas para educación física.',
        precio: 95.90,
        categoria: 'Calzado',
        genero: 'Unisex',
        imagenes: [
            'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500'
        ],
        tallas: ['32', '33', '34', '35', '36', '37', '38', '39', '40'],
        colores: [
            { nombre: 'Blanco', hexCode: '#FFFFFF' },
            { nombre: 'Negro', hexCode: '#000000' }
        ],
        stock: 30,
        nuevo: true,
        destacado: false,
        rating: 4.8,
        reviews: 38,
        marca: 'SportFeet',
        material: 'Textil y sintético'
    },

    // ROPA CASUAL - MUJER
    {
        id: 9,
        nombre: 'Blusa Casual Manga Larga',
        descripcion: 'Blusa casual elegante, perfecta para ocasiones informales. Diseño moderno y cómodo.',
        precio: 58.90,
        categoria: 'Casual',
        subcategoria: 'Blusas',
        genero: 'Mujer',
        imagenes: [
            'https://images.unsplash.com/photo-1485968579580-b6d095142e6e?w=500'
        ],
        tallas: ['S', 'M', 'L', 'XL'],
        colores: [
            { nombre: 'Blanco', hexCode: '#FFFFFF' },
            { nombre: 'Rosa', hexCode: '#FFC0CB' },
            { nombre: 'Celeste', hexCode: '#A8D8EA' }
        ],
        stock: 45,
        nuevo: true,
        destacado: false,
        rating: 4.3,
        reviews: 15,
        marca: 'CasualStyle'
    },
    {
        id: 10,
        nombre: 'Jean Skinny Azul',
        descripcion: 'Jean de corte skinny en denim de alta calidad. Diseño moderno y versátil.',
        precio: 89.90,
        categoria: 'Casual',
        subcategoria: 'Jeans',
        genero: 'Mujer',
        imagenes: [
            'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500'
        ],
        tallas: ['6', '8', '10', '12', '14'],
        colores: [
            { nombre: 'Azul Claro', hexCode: '#87CEEB' },
            { nombre: 'Azul Oscuro', hexCode: '#00008B' }
        ],
        stock: 35,
        nuevo: false,
        destacado: true,
        rating: 4.7,
        reviews: 62,
        marca: 'DenimCo'
    },

    // ACCESORIOS
    {
        id: 11,
        nombre: 'Mochila Escolar Reforzada',
        descripcion: 'Mochila escolar con compartimentos organizadores, respaldo acolchado y correas ajustables.',
        precio: 125.90,
        precioAnterior: 159.90,
        descuento: 21,
        categoria: 'Accesorios',
        genero: 'Unisex',
        imagenes: [
            'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500'
        ],
        tallas: ['Única'],
        colores: [
            { nombre: 'Azul', hexCode: '#0000FF' },
            { nombre: 'Negro', hexCode: '#000000' },
            { nombre: 'Gris', hexCode: '#808080' }
        ],
        stock: 40,
        nuevo: true,
        destacado: true,
        rating: 4.9,
        reviews: 87,
        marca: 'BackPack Pro'
    },
    {
        id: 12,
        nombre: 'Medias Escolares Pack x3',
        descripcion: 'Pack de 3 pares de medias escolares de algodón. Refuerzo en talón y punta.',
        precio: 24.90,
        categoria: 'Accesorios',
        genero: 'Unisex',
        imagenes: [
            'https://images.unsplash.com/photo-1586350977771-b3b0abd50c82?w=500'
        ],
        tallas: ['S', 'M', 'L'],
        colores: [
            { nombre: 'Blanco', hexCode: '#FFFFFF' },
            { nombre: 'Azul Marino', hexCode: '#000080' }
        ],
        stock: 100,
        nuevo: false,
        destacado: false,
        rating: 4.4,
        reviews: 45,
        marca: 'SocksPro'
    }
];

// Categorías para filtros
export const CATEGORIAS = [
    'Uniformes Escolares',
    'Buzos Escolares',
    'Calzado',
    'Casual',
    'Accesorios'
];

export const GENEROS = ['Mujer', 'Hombre', 'Unisex'];

export const TALLAS_ROPA = ['6', '8', '10', '12', '14', '16', 'S', 'M', 'L', 'XL'];
export const TALLAS_CALZADO = ['32', '33', '34', '35', '36', '37', '38', '39', '40'];

export interface ProductAdminListResponse {
    productId: number;
    variantId: number;
    imageUrl: string;
    title: string;
    categoria: string;
    categoriaId?: number;
    price: number;
    estado: string;
    color: string;
    size: string;
    sku: string;
    stock: number;
}

export interface ColorData {
    nombreColor: string;
    imagenesColor: string[];
    sizesColor: string[];
    price: number;
    discountedPrice: number;
}

export interface CreateProductRequest {
    title: string;
    description: string;
    price: number;
    discountedPrice: number;
    stockTotal: number;
    marca: string;
    material: string;
    genero: string;
    categoriaId: number;
    coloresData: ColorData[];
    nuevo: boolean;
    destacado: boolean;
    fechaInicioDescuento?: string;
    fechaFinDescuento?: string;
}

export interface SingleVariantResponse {
    productId: number;
    title: string;
    description: string;
    marca: string;
    material: string;
    genero: string;
    categoria: string;
    categoriaId?: number;
    nuevo: boolean;
    destacado: boolean;
    variantId: number;
    color: string;
    size: string;
    sku: string;
    stock: number;
    price: number;
    discountedPrice: number;
    imageUrls: string[];
    activo: boolean;
    fechaInicioDescuento?: string;
    fechaFinDescuento?: string;
}

export interface UpdateVariantRequest {
    color: string;
    size: string;
    stock: number;
    price: number;
    discountedPrice: number;
    imageUrls: string[];
    fechaInicioDescuento?: string;
    fechaFinDescuento?: string;
}

export interface UpdateProductBasicRequest {
    title: string;
    description: string;
    marca: string;
    material: string;
    genero: string;
    categoriaId: number;
    nuevo: boolean;
    destacado: boolean;
}

export interface ProductCardResponse {
    id: number;
    title: string;
    marca: string;
    price: number;
    discountedPrice: number;
    discountPercent: number;
    nuevo: boolean;
    mainImageUrl: string;
    availableColors: string[];
    availableSizes?: string[];
    suggestedSize?: string;
    categoryName?: string;
    stock: number;
}

export function mapProductToCardResponse(p: Product): ProductCardResponse {
    return {
        id: p.id,
        title: p.nombre,
        marca: p.marca || '',
        price: p.precio,
        discountedPrice: p.precioAnterior || 0,
        discountPercent: p.descuento || 0,
        nuevo: p.nuevo,
        mainImageUrl: p.imagenes && p.imagenes.length > 0 ? p.imagenes[0] : '',
        availableColors: p.colores ? p.colores.map(c => c.nombre) : [],
        stock: p.stock ?? 0
    };
}