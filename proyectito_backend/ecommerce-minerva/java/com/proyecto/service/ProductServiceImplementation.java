package com.proyecto.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.Categoria;
import com.proyecto.model.Product;
import com.proyecto.model.ProductVariant;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.CategoryRepository;
import com.proyecto.repositories.ProductRepository;
import com.proyecto.repositories.ProductVariantRepository;
import com.proyecto.request.CreateProductRequest;
import com.proyecto.request.UpdateProductBasicRequest;
import com.proyecto.request.UpdateVariantRequest;
import com.proyecto.response.PaginatedResponse;
import com.proyecto.response.ProductAdminListResponse;
import com.proyecto.response.ProductCardResponse;

@Service
public class ProductServiceImplementation implements ProductService{

	private ProductRepository productRepository;
	private UserService userService;
	private CategoryRepository categoryRepository;
	private ProductVariantRepository variantRepository;

	public ProductServiceImplementation(ProductRepository productRepository,
            UserService userService,
            CategoryRepository categoryRepository,
            ProductVariantRepository variantRepository) {
		this.productRepository = productRepository;
		this.userService = userService;
		this.categoryRepository = categoryRepository;
		this.variantRepository = variantRepository;
		}

//	@Override
//	public void generateSampleProducts(int numberOfProducts) {
//	    // Primero, crear categorías variadas
//	    List<Categoria> categorias = createSampleCategories();
//
//	    // Lista de colores predefinidos
//	    List<String> colors = Arrays.asList("Rojo", "Azul", "Negro", "Blanco", "Verde");
//
//	    // Lista de tallas predefinidas
//	    List<String> sizeNames = Arrays.asList("S", "M", "L", "XL");
//
//	    // Lista de marcas
//	    List<String> marcas = Arrays.asList("Nike", "Adidas", "Puma", "Reebok", "Under Armour");
//
//	    Random random = new Random();
//
//	    for (int i = 1; i <= numberOfProducts; i++) {
//	        Product product = new Product();
//
//	        // Datos básicos del producto
//	        product.setTitle("Producto " + i);
//	        product.setDescription("Descripción detallada del producto " + i);
//
//	        // Asignar color aleatorio
//	        product.setColor(colors.get(random.nextInt(colors.size())));
//
//	        // Precios y descuentos realistas
//	        int basePrice = 50 + (random.nextInt(20) * 10); // Precios entre 50 y 250
//	        product.setPrice(basePrice);
//	        int discount = random.nextInt(50); // Descuento entre 0% y 50%
//	        product.setDescuentot(discount);
//	        product.setDescuentoprice(basePrice - (basePrice * discount / 100));
//
//	        // URL de imagen
//	        product.setImageUrl("https://example.com/product-" + i + ".jpg");
//
//	        // Marca aleatoria
//	        product.setMarca(marcas.get(random.nextInt(marcas.size())));
//
//	        // Generar conjunto de tallas con cantidades variables
//	        Set<Size> sizes = new HashSet<>();
//	        int numberOfSizes = 1 + random.nextInt(3); // 1-3 tallas por producto
//
//	        for (int j = 0; j < numberOfSizes; j++) {
//	            Size size = new Size();
//	            size.setName(sizeNames.get(random.nextInt(sizeNames.size())));
//	            size.setCantidad(random.nextInt(50)); // 0-49 unidades por talla
//	            sizes.add(size);
//	        }
//	        product.setSizes(sizes);
//
//	        // Establecer cantidad total
//	        int totalQuantity = sizes.stream()
//	                              .mapToInt(Size::getCantidad)
//	                              .sum();
//	        product.setCantidad(totalQuantity);
//
//	        // Asignar categoría aleatoria
//	        product.setCategoria(categorias.get(random.nextInt(categorias.size())));
//
//	        // Establecer fecha de creación
//	        product.setCreatedAt(LocalDateTime.now());
//
//	        // Guardar el producto
//	        productRepository.save(product);
//	    }
//	}
	private int calcularPorcentaje(int precioActual, int precioAnterior) {
        if (precioAnterior > precioActual && precioAnterior > 0) {
            double diferencia = precioAnterior - precioActual;
            return (int) Math.round((diferencia / precioAnterior) * 100);
        }
        return 0;
    }

	private void actualizarDescuentoMaximoDelPadre(Product product) {
        int maxDescuento = 0;

        if (product.getVariantes() != null && !product.getVariantes().isEmpty()) {
            for (ProductVariant v : product.getVariantes()) {
                if (v.getDescuentot() > maxDescuento) {
                    maxDescuento = v.getDescuentot();
                }
            }
        } else {
            maxDescuento = calcularPorcentaje(product.getPrice(), product.getDescuentoprice());
        }

        product.setDescuentot(maxDescuento);
    }

	// Método helper para capitalizar la primera letra
    private String formatearTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return texto;
        }
        texto = texto.trim();
        // Convierte la primera letra a mayúscula y deja el resto tal cual
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }

    @Override
    @Transactional
    public ProductVariant updateVariant(Long variantId, UpdateVariantRequest req) throws ProductException {
        // 1. Buscamos solo la variante específica
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new ProductException("Variante no encontrada con el id " + variantId));

        // 2. Actualizamos solo los datos que vengan en la petición
        if (req.getColor() != null) {
			variant.setColor(req.getColor());
		}
        if (req.getSize() != null) {
			variant.setSize(req.getSize());
		}
        if (req.getStock() >= 0) {
			variant.setStock(req.getStock());
		}
        if (req.getPrice() >= 0) {
			variant.setPrice(req.getPrice());
		}
        if (req.getDescuentoprice() >= 0) {
			variant.setDescuentoprice(req.getDescuentoprice());
		}

        variant.setDescuentot(calcularPorcentaje(variant.getPrice(), variant.getDescuentoprice()));
        variant.setFechaInicioDescuento(req.getFechaInicioDescuento());
        variant.setFechaFinDescuento(req.getFechaFinDescuento());

        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            variant.getImageUrls().clear(); // Borramos las fotos anteriores de esta variante
            variant.getImageUrls().addAll(req.getImageUrls());
        }

        variant = variantRepository.save(variant);

        Product productPadre = variant.getProduct();

        			actualizarDescuentoMaximoDelPadre(productPadre);

        productRepository.save(productPadre); // Guardamos el padre actualizado

        return variant;
    }
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductAdminListResponse> getPaginatedVariants(int pageNumber, int pageSize) {

        // Creamos la petición de paginación (Spring Data JPA cuenta desde la página 0)
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Obtenemos las variantes de la base de datos
        Page<ProductVariant> variantPage = variantRepository.findAllVariantsWithProduct(pageable);

        // Mapeamos cada Variant a nuestro DTO de respuesta
        List<ProductAdminListResponse> dtos = variantPage.getContent().stream()
            .map(variant -> new ProductAdminListResponse(variant.getProduct(), variant))
            .collect(java.util.stream.Collectors.toList());

        // Armamos el objeto de respuesta paginada para Angular
        PaginatedResponse<ProductAdminListResponse> response = new PaginatedResponse<>();
        response.setContent(dtos);
        response.setPageNumber(variantPage.getNumber());
        response.setPageSize(variantPage.getSize());
        response.setTotalElements(variantPage.getTotalElements());
        response.setTotalPages(variantPage.getTotalPages());
        response.setLast(variantPage.isLast());

        return response;
    }
	private List<Categoria> createSampleCategories() {
	    List<String> categoryNames = Arrays.asList(
	        "Ropa Deportiva",
	        "Calzado",
	        "Accesorios",
	        "Electrónicos",
	        "Hogar"
	    );

	    List<Categoria> categorias = new ArrayList<>();

	    for (String name : categoryNames) {
	        Categoria categoria = categoryRepository.findByName(name);
	        if (categoria == null) {
	            categoria = new Categoria();
	            categoria.setName(name);
	            categoria.setLevel(1);
	            categoria = categoryRepository.save(categoria);
	        }
	        categorias.add(categoria);
	    }

    return categorias;
}

	@Override
    @Transactional
    public Product createProduct(CreateProductRequest req, Usuario creador) {

        Categoria categoria = categoryRepository.findByName(req.getCategoria());
        if(categoria == null) {
            categoria = new Categoria();
            categoria.setName(req.getCategoria());
            categoria.setLevel(1);
            categoria = categoryRepository.save(categoria);
        }

        // 2. Crear el Producto Principal (Precio Base)
        Product product = new Product();
        product.setTitle(formatearTexto(req.getTitle()));
        product.setDescription(formatearTexto(req.getDescription()));
        product.setPrice(req.getPrice()); // Precio Base
        product.setDescuentoprice(req.getDescuentoprice()); // Descuento Base

        product.setFechaInicioDescuento(req.getFechaInicioDescuento());
        product.setFechaFinDescuento(req.getFechaFinDescuento());

        product.setMarca(formatearTexto(req.getMarca()));
        product.setMaterial(formatearTexto(req.getMaterial()));
        product.setGenero(req.getGenero());

        product.setNuevo(req.isNuevo());
        product.setDestacado(req.isDestacado());
        product.setCategoria(categoria);
        product.setCreatedAt(LocalDateTime.now());
        product.setCreadoPor(creador);

        if (req.getColoresData() != null && !req.getColoresData().isEmpty()) {

            int numVariantesTotales = 0;
            for(CreateProductRequest.ColorData colorData : req.getColoresData()) {
                if (colorData.getSizesColor() != null) {
                    numVariantesTotales += colorData.getSizesColor().size();
                }
            }

            int stockPorVariante = numVariantesTotales > 0 ? req.getStockTotal() / numVariantesTotales : 0;

            for(CreateProductRequest.ColorData colorData : req.getColoresData()) {

                if (colorData.getSizesColor() != null) {
                    for(String size : colorData.getSizesColor()) {
                        ProductVariant variant = new ProductVariant();
                        variant.setColor(colorData.getNombreColor());
                        variant.setSize(size);
                        variant.setStock(stockPorVariante);
                        variant.setProduct(product);

                        if (colorData.getPrice() > 0) {
                            variant.setPrice(colorData.getPrice());
                            variant.setDescuentoprice(colorData.getDescuentoprice());
                            variant.setFechaInicioDescuento(colorData.getFechaInicioDescuento());
                            variant.setFechaFinDescuento(colorData.getFechaFinDescuento());
                        } else {
                            variant.setPrice(product.getPrice());
                            variant.setDescuentoprice(product.getDescuentoprice());
                            variant.setFechaInicioDescuento(product.getFechaInicioDescuento());
                            variant.setFechaFinDescuento(product.getFechaFinDescuento());
                        }

                        variant.setDescuentot(calcularPorcentaje(variant.getPrice(), variant.getDescuentoprice()));
                        if (colorData.getImagenesColor() != null && !colorData.getImagenesColor().isEmpty()) {
                            variant.getImageUrls().addAll(colorData.getImagenesColor());
                        }
                        product.getVariantes().add(variant);
                    }
                }
            }
        }
        actualizarDescuentoMaximoDelPadre(product);
        return productRepository.save(product);
    }

	@Override
    @Transactional
    public Product updateProductBasicInfo(Long productId, UpdateProductBasicRequest req) throws ProductException {
        Product product = findProductById(productId);

        // Actualizamos SOLO los textos y categorías
        if (req.getTitle() != null) {
			product.setTitle(formatearTexto(req.getTitle()));
		}
        if (req.getDescription() != null) {
			product.setDescription(formatearTexto(req.getDescription()));
		}
        if (req.getMarca() != null) {
			product.setMarca(formatearTexto(req.getMarca()));
		}
        if (req.getMaterial() != null) {
			product.setMaterial(formatearTexto(req.getMaterial()));
		}
        if (req.getGenero() != null) {
			product.setGenero(req.getGenero());
		}

        product.setNuevo(req.isNuevo());
        product.setDestacado(req.isDestacado());

        // Manejo de Categoría (igual que antes)
        if (req.getCategoria() != null && (product.getCategoria() == null || !req.getCategoria().equals(product.getCategoria().getName()))) {
            Categoria categoria = categoryRepository.findByName(req.getCategoria());
            if(categoria == null) {
                categoria = new Categoria();
                categoria.setName(req.getCategoria());
                categoria.setLevel(3);
                categoria = categoryRepository.save(categoria);
            }
            product.setCategoria(categoria);
        }

        // GUARDAMOS. Las variantes que ya existen asociadas a este producto NO se borran ni se alteran.
        return productRepository.save(product);
    }
	@Override
    @Transactional
    public String toggleVariantStatus(Long variantId) throws ProductException {
        // Buscamos la variante
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new ProductException("Variante no encontrada con el id " + variantId));

        boolean nuevoEstado = !variant.isActivo();
        variant.setActivo(nuevoEstado);

        variantRepository.save(variant);

        return nuevoEstado ? "Variante reactivada correctamente" : "Variante desactivada (Soft Delete) correctamente";
    }

	@Override
    @Transactional(readOnly = true)
    public ProductVariant getVariantById(Long variantId) throws ProductException {
        return variantRepository.findById(variantId)
            .orElseThrow(() -> new ProductException("Variante no encontrada con el id " + variantId));
    }

//	@Override
//    @Transactional
//    public Product updateProduct(Long productId, CreateProductRequest req) throws ProductException {
//        Product product = findProductById(productId);
//
//        // Actualizamos campos básicos
//        if (req.getTitle() != null) product.setTitle(formatearTexto(req.getTitle()));
//        if (req.getDescription() != null) product.setDescription(formatearTexto(req.getDescription()));
//        if (req.getPrice() > 0) product.setPrice(req.getPrice());
//        if (req.getDescuentoprice() >= 0) product.setDescuentoprice(req.getDescuentoprice());
//        if (req.getMarca() != null) product.setMarca(formatearTexto(req.getMarca()));
//        if (req.getMaterial() != null) product.setMaterial(formatearTexto(req.getMaterial()));
//        if (req.getGenero() != null) product.setGenero(req.getGenero());
//
//        product.setNuevo(req.isNuevo());
//        product.setDestacado(req.isDestacado());
//
//        if (req.getCategoria() != null && !req.getCategoria().equals(product.getCategoria().getName())) {
//            Categoria categoria = categoryRepository.findByName(req.getCategoria());
//            if(categoria == null) {
//                categoria = new Categoria();
//                categoria.setName(req.getCategoria());
//                categoria.setLevel(3);
//                categoria = categoryRepository.save(categoria);
//            }
//            product.setCategoria(categoria);
//        }
//
//        // --- MANEJO DE VARIANTES ---
//        if (req.getColoresData() != null && !req.getColoresData().isEmpty()) {
//
//            product.getVariantes().clear();
//
//            int numVariantesTotales = 0;
//            for(CreateProductRequest.ColorData colorData : req.getColoresData()) {
//                if (colorData.getSizesColor() != null) {
//                    numVariantesTotales += colorData.getSizesColor().size();
//                }
//            }
//
//            int stockPorVariante = numVariantesTotales > 0 ? req.getStockTotal() / numVariantesTotales : 0;
//
//            for(CreateProductRequest.ColorData colorData : req.getColoresData()) {
//
//                if (colorData.getSizesColor() != null) {
//
//                    for(String size : colorData.getSizesColor()) {
//                        ProductVariant variant = new ProductVariant();
//                        variant.setColor(colorData.getNombreColor());
//                        variant.setSize(size);
//                        variant.setStock(stockPorVariante);
//                        variant.setProduct(product);
//
//                        // ✅ NUEVO: LÓGICA DE PRECIOS CON FALLBACK AL ACTUALIZAR
//                        if (colorData.getPrice() > 0) {
//                            variant.setPrice(colorData.getPrice());
//                            variant.setDescuentoprice(colorData.getDescuentoprice());
//                        } else {
//                            variant.setPrice(product.getPrice());
//                            variant.setDescuentoprice(product.getDescuentoprice());
//                        }
//
//                        if (colorData.getImagenesColor() != null && !colorData.getImagenesColor().isEmpty()) {
//                            variant.getImageUrls().addAll(colorData.getImagenesColor());
//                        }
//
//                        product.getVariantes().add(variant);
//                    }
//                }
//            }
//        }
//
//        return productRepository.save(product);
//    }
//
	@Override
    public Product findProductById(Long id) throws ProductException {
        Optional<Product> opt = productRepository.findById(id);

        if (opt.isPresent()) {
            Product product = opt.get();
            // Inicializa la colección de variantes si es LAZY (antes decía getSizes())
            product.getVariantes().size();
            return product;
        }
        throw new ProductException("Producto no encontrado con el id " + id);
    }

	@Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductCardResponse> getAllProductPublic( 
            String categoria, String genero, Boolean isNuevo, 
            List<String> colors, List<String> sizes,
            Integer minPrice, Integer maxPrice, Integer minDiscount,
            String sort, String stock, Integer pageNumber, Integer pageSize) {
            
        // Validaciones de negocio
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor que el máximo");
        }

        // ✅ 1. CREAR LA REGLA DE ORDENAMIENTO (Sort)
        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt"); // Por defecto: Más recientes
        
        if ("precio_menor".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "price");
        } else if ("precio_mayor".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "price");
        } else if ("relevancia".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt"); // Puedes ajustarlo después si tienes un sistema de vistas
        }
        
        // ✅ 2. AGREGAR EL SORT AL PAGEABLE
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortObj);
        
        // ✅ 3. LLAMAR AL REPO (Fíjate que ya NO le pasamos la variable 'sort')
        Page<Product> page = productRepository.filterProducts(
                categoria, genero, isNuevo, colors, sizes, minPrice, maxPrice,
                minDiscount, stock, pageable);
                
        // 4. MAPEO: Convertir Product (Entidad pesada) a ProductCardResponse (DTO liviano)
        List<ProductCardResponse> cardList = page.getContent().stream().map(p -> {
            ProductCardResponse card = new ProductCardResponse();
            card.setId(p.getId());
            card.setTitle(p.getTitle());
            card.setMarca(p.getMarca());
            card.setPrice(p.getPrice());
            card.setDescuentoprice(p.getDescuentoprice());
            card.setDescuentot(p.getDescuentot());
            card.setNuevo(p.isNuevo());

            // Lógica para obtener la imagen principal y los colores
            if (p.getVariantes() != null && !p.getVariantes().isEmpty()) {
                
                List<String> coloresDisponibles = p.getVariantes().stream()
                    .map(v -> v.getColor())
                    .distinct() 
                    .collect(java.util.stream.Collectors.toList());
                card.setAvailableColors(coloresDisponibles);
                
                ProductVariant primeraVariante = p.getVariantes().iterator().next();
                if (primeraVariante.getImageUrls() != null && !primeraVariante.getImageUrls().isEmpty()) {
                    card.setMainImageUrl(primeraVariante.getImageUrls().get(0));
                }
            }
            return card;
        }).collect(java.util.stream.Collectors.toList());
                
        // 5. EMPAQUETAR Y DEVOLVER
        PaginatedResponse<ProductCardResponse> response = new PaginatedResponse<>();
        response.setContent(cardList);
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());
        
        return response;
    }

    @Override
    public List<Product> findAllProducts() {
        return productRepository.findAllProductsOptimized();
    }
}


