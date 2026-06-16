package com.proyecto.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.proyecto.model.Color;
import com.proyecto.model.Talla;
import com.proyecto.model.Usuario;
import com.proyecto.model.Categoria;
import com.proyecto.model.Coupon;
import com.proyecto.model.SmartCartConfig;
import com.proyecto.repositories.ColorRepository;
import com.proyecto.repositories.ProductRepository;
import com.proyecto.repositories.SmartCartConfigRepository;
import com.proyecto.repositories.TallaRepository;
import com.proyecto.repositories.UserRepository;
import com.proyecto.repositories.CategoryRepository;
import com.proyecto.repositories.CouponRepository;
import com.proyecto.request.CreateProductRequest;
import com.proyecto.service.ProductService;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final ColorRepository colorRepository;
    private final TallaRepository tallaRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final CouponRepository couponRepository;
    private final SmartCartConfigRepository smartCartConfigRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public DataInitializer(ColorRepository colorRepository, TallaRepository tallaRepository,
                           UserRepository userRepository, ProductRepository productRepository,
                           ProductService productService, PasswordEncoder passwordEncoder,
                           CategoryRepository categoryRepository, CouponRepository couponRepository,
                           SmartCartConfigRepository smartCartConfigRepository) {
        this.colorRepository = colorRepository;
        this.tallaRepository = tallaRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.couponRepository = couponRepository;
        this.smartCartConfigRepository = smartCartConfigRepository;
    }

    private void cleanOldDatabaseData() {
        logger.info("Checking database for old mockup absolute HTTP URLs...");
        try {
            Number countNum = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM variant_images WHERE image_url LIKE 'http%'"
            ).getSingleResult();
            
            if (countNum != null && countNum.intValue() > 0) {
                logger.warn("Found {} absolute HTTP/HTTPS mockup URLs. Commencing safe cascade cleanup...", countNum.intValue());
                
                entityManager.createNativeQuery("DELETE FROM variant_images").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM cart_item").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM order_item").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM orders").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM product_variant").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM product").executeUpdate();
                
                logger.info("Database cleanup completed successfully.");
            } else {
                logger.info("No old HTTP/HTTPS mockup URLs found.");
            }
        } catch (Exception e) {
            logger.error("Failed to perform self-healing database cleanup. Proceeding normally...", e);
        }
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        cleanOldDatabaseData();
        initializeCategories();
        System.out.println("=========================================");
        System.out.println("¡SI ESTOY ENTRANDO AL DATA INITIALIZER!");
        System.out.println("=========================================");
        
        logger.info("Initializing database with default data...");
        
        // Migrate welcome coupons for any existing users
        migrateWelcomeCoupons();

        // Initialize Colors
        List<Color> defaultColors = Arrays.asList(
            createColor("Rojo", "#FF0000"),
            createColor("Azul", "#0000FF"),
            createColor("Verde", "#008000"),
            createColor("Negro", "#000000"),
            createColor("Blanco", "#FFFFFF"),
            createColor("Gris", "#808080"),
            createColor("Amarillo", "#FFFF00")
        );
        for (Color c : defaultColors) {
            if (colorRepository.findByNombre(c.getNombre()) == null) {
                colorRepository.save(c);
            }
        }

        // Initialize Sizes
        List<Talla> defaultTallas = Arrays.asList(
            createTalla("XS", "General"),
            createTalla("S", "General"),
            createTalla("M", "General"),
            createTalla("L", "General"),
            createTalla("XL", "General"),
            createTalla("XXL", "General"),
            createTalla("38", "Calzado"),
            createTalla("39", "Calzado"),
            createTalla("40", "Calzado"),
            createTalla("41", "Calzado"),
            createTalla("42", "Calzado")
        );
        for (Talla t : defaultTallas) {
            if (tallaRepository.findByValor(t.getValor()) == null) {
                tallaRepository.save(t);
            }
        }

        // Initialize Admin User for product creation
        Usuario adminUser = userRepository.findByEmail("admin@ripleysito.com");
        if (adminUser == null) {
            adminUser = new Usuario();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Ripleysito");
            adminUser.setEmail("admin@ripleysito.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole(Usuario.UserRole.ADMIN);
            adminUser = userRepository.save(adminUser);
            logger.info("Created default admin user: admin@ripleysito.com");
        }

        // Initialize Employee User
        Usuario employeeUser = userRepository.findByEmail("empleado@ripleysito.com");
        if (employeeUser == null) {
            employeeUser = new Usuario();
            employeeUser.setFirstName("Empleado");
            employeeUser.setLastName("Ripleysito");
            employeeUser.setEmail("empleado@ripleysito.com");
            employeeUser.setPassword(passwordEncoder.encode("empleado123"));
            employeeUser.setRole(Usuario.UserRole.EMPLOYEE);
            userRepository.save(employeeUser);
            logger.info("Created default employee user: empleado@ripleysito.com");
        }

        // Initialize 20 Mixed Products
        if (productRepository.count() < 20) {
            logger.info("Generating 20 mock products...");
            generateMockProducts(adminUser);
            logger.info("20 mock products generated successfully.");
        }

        // Initialize SmartCartConfig
        if (smartCartConfigRepository.count() == 0) {
            SmartCartConfig config = new SmartCartConfig();
            config.setActive(true);
            config.setUpsellDiscountPercentage(10);
            config.setMaxProductsToShow(4);
            config.setRecommendationCriteria("MOST_SOLD");
            config.setMinStockRequired(3);
            smartCartConfigRepository.save(config);
            logger.info("Created default Smart Cart Configuration.");
        }
    }

    private void generateMockProducts(Usuario admin) {
        String baseUrl = "uploads/";
        
        List<Talla> allTallas = tallaRepository.findAll();
        List<String> tallasRopa = new ArrayList<>();
        List<String> tallasZapatos = new ArrayList<>();
        for (Talla t : allTallas) {
            if (t.getTipo().equals("General")) tallasRopa.add(t.getValor());
            if (t.getTipo().equals("Calzado")) tallasZapatos.add(t.getValor());
        }

        // Producto 1: Polo Nike
        CreateProductRequest p1 = new CreateProductRequest();
        p1.setTitle("Polo Nike Dri-FIT");
        p1.setDescription("Polo deportivo transpirable ideal para correr y entrenar. Diseño ergonómico de alta calidad.");
        p1.setPrice(120);
        p1.setDiscountedPrice(99);
        p1.setStockTotal(100);
        p1.setMarca("Nike");
        p1.setMaterial("Poliéster");
        p1.setGenero("Hombre");
        Categoria cat1 = categoryRepository.findByName("Polos y tops");
        if (cat1 != null) p1.setCategoriaId(cat1.getId());
        p1.setNuevo(true);
        p1.setDestacado(true);
        
        List<CreateProductRequest.ColorData> c1 = new ArrayList<>();
        // Negro con 3 tomas de diferentes ángulos para el carrusel
        c1.add(createColorDataMulti("Negro", Arrays.asList(
            baseUrl + "polo_nike_negro.png",
            baseUrl + "polo_nike_negro_back.png",
            baseUrl + "polo_nike_negro_detail.png"
        ), 120, tallasRopa));
        c1.add(createColorData("Blanco", baseUrl + "polo_nike_blanco.png", 120, tallasRopa));
        c1.add(createColorData("Rojo", baseUrl + "polo_nike_rojo.png", 120, tallasRopa));
        p1.setColoresData(c1);
        
        try {
            productService.createProduct(p1, admin);
        } catch (Exception e) {
            logger.error("Error creating mock product 1", e);
        }

        // Producto 2: Zapatillas Adidas
        CreateProductRequest p2 = new CreateProductRequest();
        p2.setTitle("Zapatillas Adidas Ultraboost");
        p2.setDescription("Zapatillas de running con máxima amortiguación y retorno de energía. Comodidad absoluta para tus pies.");
        p2.setPrice(450);
        p2.setDiscountedPrice(399);
        p2.setStockTotal(50);
        p2.setMarca("Adidas");
        p2.setMaterial("Malla técnica");
        p2.setGenero("Unisex");
        Categoria cat2 = categoryRepository.findByName("Zapatillas deportivas");
        if (cat2 == null) cat2 = categoryRepository.findByName("Deportivas");
        if (cat2 != null) p2.setCategoriaId(cat2.getId());
        p2.setNuevo(true);
        p2.setDestacado(true);
        
        List<CreateProductRequest.ColorData> c2 = new ArrayList<>();
        c2.add(createColorData("Negro", baseUrl + "zapatilla_adidas_negra.png", 450, tallasZapatos));
        c2.add(createColorData("Blanco", baseUrl + "zapatilla_adidas_blanca.png", 450, tallasZapatos));
        p2.setColoresData(c2);
        
        try {
            productService.createProduct(p2, admin);
        } catch (Exception e) {
            logger.error("Error creating mock product 2", e);
        }
        
        // Generar 18 más usando un bucle con marcas reales
        String[] marcas = {"Nike", "Adidas", "Puma", "Reebok", "Under Armour", "Zara", "H&M"};
        String[] categoriasRopa = {"Polos y tops", "Casacas", "Poleras y polerones"};
        String[] categoriasZapatos = {"Zapatillas Urbanas", "Zapatillas Deportivas"};
        
        Random r = new Random();
        for(int i = 3; i <= 20; i++) {
            boolean isZapatos = r.nextBoolean();
            String marca = marcas[r.nextInt(marcas.length)];
            String categoria = isZapatos ? categoriasZapatos[r.nextInt(categoriasZapatos.length)] : categoriasRopa[r.nextInt(categoriasRopa.length)];
            
            CreateProductRequest px = new CreateProductRequest();
            px.setTitle((isZapatos ? "Zapatillas " : "Polo ") + marca + " Essential " + i);
            px.setDescription("Excelente producto de " + marca + " para tu día a día. Comodidad y estilo garantizados con los mejores materiales.");
            int price = 100 + r.nextInt(300);
            px.setPrice(price);
            px.setDiscountedPrice(price - r.nextInt(50));
            px.setStockTotal(r.nextInt(50) + 10);
            px.setMarca(marca);
            px.setMaterial(isZapatos ? "Sintético/Malla" : "100% Algodón");
            px.setGenero(r.nextBoolean() ? "Hombre" : "Mujer");
            Categoria catx = categoryRepository.findByName(categoria);
            if (catx == null) catx = categoryRepository.findByName("Deportivas");
            if (catx != null) px.setCategoriaId(catx.getId());
            px.setNuevo(r.nextBoolean());
            px.setDestacado(r.nextBoolean());
            
            List<CreateProductRequest.ColorData> cx = new ArrayList<>();
            if (isZapatos) {
                cx.add(createColorData("Negro", baseUrl + "zapatilla_adidas_negra.png", price, tallasZapatos));
                if(r.nextBoolean()) cx.add(createColorData("Blanco", baseUrl + "zapatilla_adidas_blanca.png", price, tallasZapatos));
            } else {
                cx.add(createColorData("Negro", baseUrl + "polo_nike_negro.png", price, tallasRopa));
                if(r.nextBoolean()) cx.add(createColorData("Blanco", baseUrl + "polo_nike_blanco.png", price, tallasRopa));
                if(r.nextBoolean()) cx.add(createColorData("Rojo", baseUrl + "polo_nike_rojo.png", price, tallasRopa));
            }
            px.setColoresData(cx);
            
            try {
                productService.createProduct(px, admin);
            } catch (Exception e) {
                logger.error("Error creating mock product " + i, e);
            }
        }
    }

    private CreateProductRequest.ColorData createColorDataMulti(String color, List<String> imgUrls, int price, List<String> tallas) {
        CreateProductRequest.ColorData cd = new CreateProductRequest.ColorData();
        cd.setNombreColor(color);
        cd.setImagenesColor(imgUrls);
        cd.setPrice(price);
        cd.setDiscountedPrice(price);
        cd.setSizesColor(tallas);
        return cd;
    }

    private CreateProductRequest.ColorData createColorData(String color, String imgUrl, int price, List<String> tallas) {
        return createColorDataMulti(color, Arrays.asList(imgUrl), price, tallas);
    }

    private Color createColor(String nombre, String hexCode) {
        Color color = new Color();
        color.setNombre(nombre);
        color.setHexCode(hexCode);
        return color;
    }

    private Talla createTalla(String valor, String tipo) {
        Talla talla = new Talla();
        talla.setValor(valor);
        talla.setTipo(tipo);
        return talla;
    }

    private void initializeCategories() {
        logger.info("Initializing hierarchical categories...");

        // --- MUJER (L1) ---
        Categoria mujer = getOrCreateCategory("Mujer", null, 1);
        
        Categoria ropaMujer = getOrCreateCategory("Ropa Mujer", mujer, 2);
        String[] l3RopaMujer = {"Polos y tops", "Blusas", "Jeans", "Vestidos y enterizos", "Pantalones y joggers", "Faldas", "Shorts", "Cárdigans", "Casacas", "Poleras", "Blazers"};
        for (String name : l3RopaMujer) {
            getOrCreateCategory(name, ropaMujer, 3);
        }

        Categoria lenceriaMujer = getOrCreateCategory("Lencería y ropa interior", mujer, 2);
        String[] l3Lenceria = {"Pijamas y camisones", "Calzones", "Sostenes", "Panties y medias", "Batas", "Fajas y modeladores"};
        for (String name : l3Lenceria) {
            getOrCreateCategory(name, lenceriaMujer, 3);
        }

        Categoria banoMujer = getOrCreateCategory("Ropa de baño", mujer, 2);
        String[] l3Bano = {"Bikinis", "Ropa de baño entera", "Accesorios de playa", "Pareos"};
        for (String name : l3Bano) {
            getOrCreateCategory(name, banoMujer, 3);
        }

        // --- CALZADO (L1) ---
        Categoria calzado = getOrCreateCategory("Calzado", null, 1);

        Categoria zapatillas = getOrCreateCategory("Zapatillas", calzado, 2);
        String[] l3Zapatillas = {"Urbanas", "Deportivas", "Limpiadores de zapatillas"};
        for (String name : l3Zapatillas) {
            getOrCreateCategory(name, zapatillas, 3);
        }

        Categoria zapatosMujer = getOrCreateCategory("Zapatos mujer", calzado, 2);
        String[] l3ZapatosMujer = {"Zapatillas urbanas", "Zapatillas deportivas", "Zapatos de vestir", "Zapatos casuales", "Ballerinas", "Sandalias", "Pantuflas", "Botas y botines"};
        for (String name : l3ZapatosMujer) {
            getOrCreateCategory(name, zapatosMujer, 3);
        }

        Categoria zapatosHombre = getOrCreateCategory("Zapatos hombre", calzado, 2);
        String[] l3ZapatosHombre = {"Zapatillas urbanas", "Zapatillas deportivas", "Zapatos casuales", "Zapatos de vestir", "Botines", "Sandalias", "Pantuflas"};
        for (String name : l3ZapatosHombre) {
            getOrCreateCategory(name, zapatosHombre, 3);
        }

        Categoria zapatosNinos = getOrCreateCategory("Zapatos niños y niñas", calzado, 2);
        String[] l3ZapatosNinos = {"Calzado Escolar", "Zapatillas de Futbol"};
        for (String name : l3ZapatosNinos) {
            getOrCreateCategory(name, zapatosNinos, 3);
        }

        // --- HOMBRE (L1) ---
        Categoria hombre = getOrCreateCategory("Hombre", null, 1);

        Categoria ropaHombre = getOrCreateCategory("Ropa Hombre", hombre, 2);
        String[] l3RopaHombre = {"Casacas y chalecos", "Poleras y polerones", "Chompas", "Polos", "Pantalones", "Jeans", "Camisas", "Shorts", "Ropa de baño"};
        for (String name : l3RopaHombre) {
            getOrCreateCategory(name, ropaHombre, 3);
        }

        Categoria ropaFormal = getOrCreateCategory("Ropa formal", hombre, 2);
        String[] l3RopaFormal = {"Camisas de vestir", "Pantalones de vestir", "Blazers y sacos", "Ternos", "Accesorios formales"};
        for (String name : l3RopaFormal) {
            getOrCreateCategory(name, ropaFormal, 3);
        }

        Categoria ropaInteriorHombre = getOrCreateCategory("Ropa Interior", hombre, 2);
        String[] l3RopaInteriorHombre = {"Bóxers y calzoncillos", "Pijamas", "Medias"};
        for (String name : l3RopaInteriorHombre) {
            getOrCreateCategory(name, ropaInteriorHombre, 3);
        }

        logger.info("Hierarchical categories initialized successfully!");
    }

    private Categoria getOrCreateCategory(String name, Categoria parent, int level) {
        Categoria cat = categoryRepository.findByName(name);
        if (cat == null && parent != null) {
            cat = categoryRepository.findByNameAndParant(name, parent.getName());
        }

        if (cat == null) {
            cat = new Categoria();
            cat.setName(name);
            cat.setPadrecategoria(parent);
            cat.setLevel(level);
            cat.setActivo(true);
            cat = categoryRepository.save(cat);
        } else {
            // Auto-sanado: si la categoría ya existe pero tiene nivel o padre diferente, se actualiza
            boolean needsUpdate = false;
            if (cat.getLevel() != level) {
                cat.setLevel(level);
                needsUpdate = true;
            }
            if (parent == null) {
                if (cat.getPadrecategoria() != null) {
                    cat.setPadrecategoria(null);
                    needsUpdate = true;
                }
            } else {
                if (cat.getPadrecategoria() == null || !cat.getPadrecategoria().getId().equals(parent.getId())) {
                    cat.setPadrecategoria(parent);
                    needsUpdate = true;
                }
            }
            if (!cat.isActivo()) {
                cat.setActivo(true);
                needsUpdate = true;
            }
            if (needsUpdate) {
                cat = categoryRepository.save(cat);
                logger.info("Category self-healed in DB: '{}' -> updated to level {} under parent '{}'",
                            name, level, parent != null ? parent.getName() : "None");
            }
        }
        return cat;
    }

    private void migrateWelcomeCoupons() {
        List<Usuario> allUsers = userRepository.findAll();
        for (Usuario user : allUsers) {
            List<Coupon> userCoupons = couponRepository.findByUserOrUserIsNull(user);
            boolean hasWelcome = userCoupons.stream().anyMatch(c -> "Primera Compra".equals(c.getName()));
            
            if (!hasWelcome) {
                Coupon coupon = new Coupon();
                coupon.setName("Primera Compra");
                coupon.setCode("BIENVENIDO-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                coupon.setDiscount(40);
                coupon.setType("percentage");
                coupon.setStartDate(LocalDateTime.now());
                coupon.setEndDate(LocalDateTime.now().plusDays(30));
                coupon.setStatus("activo");
                coupon.setUser(user);
                coupon.setUpsellDefault(false);
                couponRepository.save(coupon);
            }
        }
        logger.info("Welcome coupons migration completed.");
    }
}


