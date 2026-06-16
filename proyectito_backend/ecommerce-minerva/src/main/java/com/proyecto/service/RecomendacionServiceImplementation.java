package com.proyecto.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.response.CandidatoRecomendacionResponse;
import com.proyecto.response.HistorialUsuarioResponse;
import com.proyecto.model.CartItem;
import com.proyecto.model.Order;
import com.proyecto.model.Product;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.OrderRepository;
import com.proyecto.repositories.ProductRepository;
import com.proyecto.repositories.RecommendationMetricRepository;
import com.proyecto.response.ProductCardResponse;
import com.proyecto.model.RecommendationMetric;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecomendacionServiceImplementation implements RecomendacionService {

        private static final Logger log = LoggerFactory.getLogger(RecomendacionServiceImplementation.class);

        private final ChatClient chatClient;
        private final ProductRepository productRepository;
        private final OrderRepository orderRepository;
        private final RecommendationMetricRepository recommendationMetricRepository;
        private final SmartCartConfigService smartCartConfigService;

        public RecomendacionServiceImplementation(ChatClient.Builder chatClientBuilder,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        RecommendationMetricRepository recommendationMetricRepository,
                        SmartCartConfigService smartCartConfigService) {
                this.chatClient = chatClientBuilder.build();
                this.productRepository = productRepository;
                this.orderRepository = orderRepository;
                this.recommendationMetricRepository = recommendationMetricRepository;
                this.smartCartConfigService = smartCartConfigService;
        }

        @Override
        @Transactional
        public List<ProductCardResponse> obtenerSugerenciasCarrito(List<CartItem> carritoActual, Usuario usuario, List<String> ignoredCategories) {
                log.info("[GROQ] obtenerSugerenciasCarrito() llamado. Usuario: {} | Items en carrito: {}",
                        usuario != null ? usuario.getEmail() : "null",
                        carritoActual != null ? carritoActual.size() : 0);

                if (carritoActual == null || carritoActual.isEmpty()) {
                        log.warn("[GROQ] Carrito vacío - retornando lista vacía sin llamar a la IA");
                        return List.of();
                }

                com.proyecto.model.SmartCartConfig config = smartCartConfigService.getConfig();
                if (!config.isActive()) {
                        log.info("[SMART CART] El módulo de sugerencias está desactivado. Retornando fallback de más vendidos.");
                        // Fallback: devolver los productos con mayor CTR como sugerencias pasivas
                        List<Product> fallback = productRepository.findAllProductsOptimized().stream()
                                .filter(p -> p.getVariantes() != null && p.getVariantes().stream().anyMatch(v -> v.getStock() > 0))
                                .sorted((a, b) -> {
                                        Optional<RecommendationMetric> mA = recommendationMetricRepository.findByProductIdCustom(a.getId());
                                        Optional<RecommendationMetric> mB = recommendationMetricRepository.findByProductIdCustom(b.getId());
                                        double ctrA = mA.map(m -> m.getImpressions() > 0 ? (m.getClicks() * 100.0 / m.getImpressions()) : 0.0).orElse(0.0);
                                        double ctrB = mB.map(m -> m.getImpressions() > 0 ? (m.getClicks() * 100.0 / m.getImpressions()) : 0.0).orElse(0.0);
                                        return Double.compare(ctrB, ctrA);
                                })
                                .limit(config.getMaxProductsToShow())
                                .collect(Collectors.toList());
                        Set<String> emptyTallas = java.util.Collections.emptySet();
                        return fallback.stream().map(p -> {
                                try { return mapToProductCardResponse(p, emptyTallas); } catch (Exception e) { return null; }
                        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
                }

                int maxProducts = config.getMaxProductsToShow();
                int minStock = config.getMinStockRequired();
                String criteria = config.getRecommendationCriteria();

                // 1. Extraer historial usando las entidades Order y OrderItem
                HistorialUsuarioResponse historial = obtenerHistorialCompras(usuario);

                // 2. Identificar categorías en el carrito y extraer tallas preferidas
                Set<String> categoriasEnCarrito = carritoActual.stream()
                                .map(item -> item.getProduct().getCategoria().getName())
                                .collect(Collectors.toSet());

                if (ignoredCategories != null && !ignoredCategories.isEmpty()) {
                        log.info("[GROQ] Categorías ignoradas por el usuario en esta sesión: {}", ignoredCategories);
                        categoriasEnCarrito.addAll(ignoredCategories);
                }

                Set<String> tallasEnCarrito = carritoActual.stream()
                                .filter(item -> item.getSize() != null)
                                .map(item -> item.getSize().toUpperCase())
                                .collect(Collectors.toSet());

                // 3. Obtener candidatos disponibles en BD (SOLO productos con stock >= minStock)
                List<Product> productosDisponibles = productRepository.findAllProductsOptimized().stream()
                                .filter(p -> p.getVariantes() != null && p.getVariantes().stream()
                                                .mapToInt(v -> v.getStock() >= minStock ? v.getStock() : 0).sum() >= minStock)
                                .collect(Collectors.toList());

                List<CandidatoRecomendacionResponse> candidatos = productosDisponibles.stream()
                                .filter(p -> !categoriasEnCarrito.contains(p.getCategoria().getName()))
                                .map(p -> {
                                        CandidatoRecomendacionResponse dto = new CandidatoRecomendacionResponse();
                                        dto.setId(p.getId());
                                        dto.setTitle(p.getTitle());
                                        dto.setCategoria(p.getCategoria().getName());
                                        dto.setMarca(p.getMarca());
                                        dto.setPrice(p.getPrice());
                                        dto.setNuevo(p.isNuevo());
                                        dto.setDiscountPercent(p.getDiscountPercent());
                                        // Calcular CTR
                                        Optional<RecommendationMetric> optMetric = recommendationMetricRepository.findByProductIdCustom(p.getId());
                                        if (optMetric.isPresent() && optMetric.get().getImpressions() > 0) {
                                                RecommendationMetric m = optMetric.get();
                                                double ctr = (m.getClicks() * 100.0) / m.getImpressions();
                                                dto.setCtr(Math.round(ctr * 10.0) / 10.0);
                                        } else {
                                                dto.setCtr(0.0);
                                        }
                                        return dto;
                                }).limit(35)
                                .collect(Collectors.toList());

                // Si al excluir categorías nos quedamos con muy pocos candidatos, permitimos duplicar categorías del carrito
                if (candidatos.size() < 15) {
                        log.info("[GROQ] Pocos candidatos sin categorías del carrito ({}) - agregando otros productos de la BD", candidatos.size());
                        Set<Long> idsExistentes = candidatos.stream().map(CandidatoRecomendacionResponse::getId).collect(Collectors.toSet());
                        for (Product p : productosDisponibles) {
                                if (!idsExistentes.contains(p.getId())) {
                                        CandidatoRecomendacionResponse dto = new CandidatoRecomendacionResponse();
                                        dto.setId(p.getId());
                                        dto.setTitle(p.getTitle());
                                        dto.setCategoria(p.getCategoria().getName());
                                        dto.setMarca(p.getMarca());
                                        dto.setPrice(p.getPrice());
                                        dto.setNuevo(p.isNuevo());
                                        dto.setDiscountPercent(p.getDiscountPercent());
                                        Optional<RecommendationMetric> optMetric = recommendationMetricRepository.findByProductIdCustom(p.getId());
                                        if (optMetric.isPresent() && optMetric.get().getImpressions() > 0) {
                                                RecommendationMetric m = optMetric.get();
                                                double ctr = (m.getClicks() * 100.0) / m.getImpressions();
                                                dto.setCtr(Math.round(ctr * 10.0) / 10.0);
                                        } else {
                                                dto.setCtr(0.0);
                                        }
                                        candidatos.add(dto);
                                        idsExistentes.add(p.getId());
                                }
                                if (candidatos.size() >= 35) {
                                        break;
                                }
                        }
                }

                List<Long> idsSugeridos = new java.util.ArrayList<>();

                if ("MOST_SOLD".equalsIgnoreCase(criteria)) {
                        log.info("[SMART CART] Usando métrica: MOST_SOLD");
                        idsSugeridos = candidatos.stream()
                                        .sorted((a, b) -> Double.compare(b.getCtr(), a.getCtr()))
                                        .limit(maxProducts)
                                        .map(CandidatoRecomendacionResponse::getId)
                                        .collect(Collectors.toList());
                } else if ("SAME_CATEGORY".equalsIgnoreCase(criteria)) {
                        log.info("[SMART CART] Usando métrica: SAME_CATEGORY");
                        idsSugeridos = productosDisponibles.stream()
                                        .filter(p -> categoriasEnCarrito.contains(p.getCategoria().getName()))
                                        .limit(maxProducts)
                                        .map(Product::getId)
                                        .collect(Collectors.toList());
                } else if ("RANDOM".equalsIgnoreCase(criteria)) {
                        log.info("[SMART CART] Usando métrica: RANDOM");
                        java.util.Collections.shuffle(candidatos);
                        idsSugeridos = candidatos.stream()
                                        .limit(maxProducts)
                                        .map(CandidatoRecomendacionResponse::getId)
                                        .collect(Collectors.toList());
                } else {
                        // Por defecto usar Inteligencia Artificial ("AI_RECOMMENDED" u otros no mapeados)
                        log.info("[SMART CART] Usando métrica por defecto: AI_RECOMMENDED");

                        // Parsear métricas del prompt configuradas en el admin
                        String rawAiMetrics = config.getAiMetrics();
                        java.util.Set<String> activeMetrics = new java.util.HashSet<>();
                        if (rawAiMetrics != null && !rawAiMetrics.isBlank()) {
                                for (String m : rawAiMetrics.split(",")) {
                                        activeMetrics.add(m.trim().toUpperCase());
                                }
                        } else {
                                // Si no hay métricas configuradas, usar las por defecto
                                activeMetrics.add("USER_HISTORY");
                                activeMetrics.add("CTR_DATA");
                        }
                        log.info("[GROQ] Métricas de prompt activas: {}", activeMetrics);

                        // 4. Mapear el carrito para la IA
                        String detallesCarrito = carritoActual.stream()
                                        .map(item -> "- " + item.getProduct().getTitle() +
                                                        " (Categoría: " + item.getProduct().getCategoria().getName() + ")")
                                        .collect(Collectors.joining("\n"));

                        // 5. Construir candidatos para el prompt incluyendo solo los datos habilitados
                        StringBuilder candidatosBuilder = new StringBuilder("[\n");
                        for (int i = 0; i < candidatos.size(); i++) {
                                CandidatoRecomendacionResponse c = candidatos.get(i);
                                StringBuilder entry = new StringBuilder();
                                entry.append("  { \"id\": ").append(c.getId());
                                entry.append(", \"nombre\": \"").append(c.getTitle()).append("\"");
                                entry.append(", \"categoria\": \"").append(c.getCategoria()).append("\"");
                                if (activeMetrics.contains("CTR_DATA")) {
                                        entry.append(", \"ctr\": ").append(c.getCtr()).append("%");
                                }
                                if (activeMetrics.contains("DISCOUNTS") && c.getDiscountPercent() > 0) {
                                        entry.append(", \"descuento\": ").append(c.getDiscountPercent()).append("%");
                                }
                                if (activeMetrics.contains("PRICE_MATCH")) {
                                        entry.append(", \"precio\": S/.").append(c.getPrice());
                                }
                                if (activeMetrics.contains("NEW_ARRIVALS") && c.isNuevo()) {
                                        entry.append(", \"nuevo\": true");
                                }
                                entry.append(" }");
                                if (i < candidatos.size() - 1) entry.append(",");
                                entry.append("\n");
                                candidatosBuilder.append(entry);
                        }
                        candidatosBuilder.append("]");
                        String candidatosStr = candidatosBuilder.toString();

                        // 6. Construir System Prompt
                        StringBuilder systemSb = new StringBuilder();
                        systemSb.append("Eres un experto en retail y cross-selling para la tienda Minerva. ");
                        systemSb.append("Analiza el carrito del cliente para seleccionar exactamente ").append(maxProducts).append(" productos complementarios de la lista de candidatos. ");
                        systemSb.append("Busca complementar el outfit o necesidad del cliente. ");
                        if (activeMetrics.contains("CTR_DATA")) {
                                systemSb.append("Prioriza los productos con mayor 'ctr' (Click-Through Rate), ya que son los más populares, pero mantén coherencia con el carrito. ");
                        }
                        if (activeMetrics.contains("DISCOUNTS")) {
                                systemSb.append("Considera también los productos con mayor descuento para impulsar las ventas en liquidación. ");
                        }
                        if (activeMetrics.contains("NEW_ARRIVALS")) {
                                systemSb.append("Da preferencia a los productos marcados como 'nuevo: true' para promover los nuevos ingresos. ");
                        }
                        if (activeMetrics.contains("PRICE_MATCH")) {
                                systemSb.append("Considera el precio de los productos en el carrito y recomienda complementos dentro de un rango de precio similar o inferior. ");
                        }
                        systemSb.append("Retorna ESTRICTAMENTE una lista JSON con solo los IDs de los productos recomendados. Ejemplo: [12, 45, 23, 8]");
                        String systemPrompt = systemSb.toString();

                        // 7. Construir User Prompt con las secciones habilitadas
                        StringBuilder userSb = new StringBuilder();
                        if (activeMetrics.contains("USER_HISTORY")) {
                                HistorialUsuarioResponse historialData = obtenerHistorialCompras(usuario);
                                userSb.append("--- HISTORIAL DEL CLIENTE ---\n");
                                userSb.append("Género: ").append(historialData.getGeneroUsuario() != null ? historialData.getGeneroUsuario() : "No especificado").append("\n");
                                userSb.append("Categorías más compradas: ").append(historialData.getCategoriasMasCompradas()).append("\n");
                                userSb.append("Marcas preferidas: ").append(historialData.getMarcasPreferidas()).append("\n\n");
                        }
                        userSb.append("--- CARRITO ACTUAL ---\n");
                        userSb.append(detallesCarrito).append("\n\n");
                        userSb.append("--- CANDIDATOS DISPONIBLES ---\n");
                        userSb.append(candidatosStr).append("\n\n");
                        userSb.append("Selecciona exactamente los IDs de los ").append(maxProducts).append(" mejores productos complementarios.");
                        String userPrompt = userSb.toString();

                        // 6. Llamada a Groq con mecanismo Try-Catch ultra robusto y fallback
                        log.info("[GROQ] Iniciando llamada a la API de Groq (LLM)...");
                        log.debug("[GROQ] System prompt: {}", systemPrompt);
                        log.debug("[GROQ] Candidatos enviados: {}", candidatos.size());

                        try {
                        // Primero intentamos usar estructurado de Spring AI
                        try {
                                log.info("[GROQ] Intentando parseo JSON estructurado con Spring AI (.entity())");
                                List<Long> estructurado = chatClient.prompt()
                                                .system(systemPrompt)
                                                .user(userPrompt)
                                                .call()
                                                .entity(new ParameterizedTypeReference<List<Long>>() {
                                                });
                                if (estructurado != null && !estructurado.isEmpty()) {
                                        idsSugeridos.addAll(estructurado);
                                        log.info("[GROQ] ✅ Parseo estructurado exitoso. IDs sugeridos: {}", idsSugeridos);
                                }
                        } catch (Exception eStructured) {
                                // Si falla el estructurado, obtenemos como String e intentamos parsear los IDs por Regex
                                log.warn("[GROQ] ⚠️ Parseo estructurado falló ({}). Intentando plaintext + Regex...", eStructured.getMessage());
                                try {
                                        String plainResponse = chatClient.prompt()
                                                        .system(systemPrompt)
                                                        .user(userPrompt)
                                                        .call()
                                                        .content();
                                        log.info("[GROQ] Respuesta en texto plano de Groq: {}", plainResponse);
                                        if (plainResponse != null && !plainResponse.isEmpty()) {
                                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b\\d+\\b");
                                                java.util.regex.Matcher matcher = pattern.matcher(plainResponse);
                                                while (matcher.find()) {
                                                        try {
                                                                long val = Long.parseLong(matcher.group());
                                                                if (!idsSugeridos.contains(val)) {
                                                                        idsSugeridos.add(val);
                                                                }
                                                        } catch (NumberFormatException nfe) {
                                                                // Ignorar
                                                        }
                                                                        if (idsSugeridos.size() >= maxProducts) {
                                                                                break;
                                                                        }
                                                                }
                                                                log.info("[GROQ] IDs extraídos por Regex: {}", idsSugeridos);
                                                        }
                                                } catch (Exception eRegex) {
                                                        log.error("[GROQ] ❌ Segundo intento (plaintext) también falló: {}", eRegex.getMessage());
                                                }
                                        }
                                } catch (Exception e) {
                                        log.error("[GROQ] ❌ Error fatal llamando al LLM (Groq / OpenAI API): {}", e.getMessage(), e);
                                }
                } // Fin bloque else "AI_RECOMMENDED"

                // Historial: solo se llama ahora si USER_HISTORY no está activo (ya se llamó arriba si está activo)
                // Fallback de negocio: Si no hay suficientes IDs
                if (idsSugeridos.size() < maxProducts) {
                        log.warn("[SMART CART] ⚠️ Solo {} IDs recuperados. Rellenando con candidatos de la BD hasta {}.", idsSugeridos.size(), maxProducts);
                        for (CandidatoRecomendacionResponse cand : candidatos) {
                                if (!idsSugeridos.contains(cand.getId())) {
                                        idsSugeridos.add(cand.getId());
                                }
                                if (idsSugeridos.size() >= maxProducts) {
                                        break;
                                }
                        }
                }
                log.info("[SMART CART] IDs finales a retornar: {}", idsSugeridos);

                // 7. Buscar en la BD los IDs que eligió OpenAI o nuestra lógica
                List<Product> productosFinales = productRepository.findAllById(idsSugeridos);

                List<ProductCardResponse> tarjetas = new java.util.ArrayList<>();
                for (Product p : productosFinales) {
                        try {
                                tarjetas.add(mapToProductCardResponse(p, tallasEnCarrito));
                        } catch (Exception eMap) {
                                log.error("[GROQ] Error mapeando producto ID {}: {}", p.getId(), eMap.getMessage());
                        }
                }

                // Si al final por algún motivo terminamos con menos productos, rellenamos directamente de la BD
                if (tarjetas.size() < maxProducts) {
                        log.warn("[SMART CART] Menos tarjetas finales de lo esperado ({}). Completando con destacados directos.", tarjetas.size());
                        Set<Long> idsMapeados = tarjetas.stream().map(ProductCardResponse::getId).collect(Collectors.toSet());
                        for (Product p : productosDisponibles) {
                                if (!idsMapeados.contains(p.getId())) {
                                        try {
                                                tarjetas.add(mapToProductCardResponse(p, tallasEnCarrito));
                                                idsMapeados.add(p.getId());
                                        } catch (Exception eMap) {
                                                // Ignorar
                                        }
                                }
                                if (tarjetas.size() >= maxProducts) {
                                        break;
                                }
                        }
                }

                return tarjetas;
        }

        // Adaptado para usar Order y OrderItem
        private HistorialUsuarioResponse obtenerHistorialCompras(Usuario usuario) {
                HistorialUsuarioResponse dto = new HistorialUsuarioResponse();

                // Llamamos a tu Query personalizado para obtener las órdenes confirmadas o
                // entregadas
                List<Order> ordenesAnteriores = orderRepository.getUsersOrders(usuario.getId());

                // Mapeo: Extraemos las categorías iterando Order -> OrderItem -> Product ->
                // Categoria
                Map<String, Long> conteoCategorias = ordenesAnteriores.stream()
                                .flatMap(o -> o.getOrderItems().stream())
                                .filter(item -> item.getProduct() != null && item.getProduct().getCategoria() != null)
                                .collect(Collectors.groupingBy(item -> item.getProduct().getCategoria().getName(),
                                                Collectors.counting()));

                // Mapeo: Extraemos las marcas iterando Order -> OrderItem -> Product -> Marca
                Map<String, Long> conteoMarcas = ordenesAnteriores.stream()
                                .flatMap(o -> o.getOrderItems().stream())
                                .filter(item -> item.getProduct() != null && item.getProduct().getMarca() != null)
                                .collect(Collectors.groupingBy(item -> item.getProduct().getMarca(),
                                                Collectors.counting()));

                List<String> topCategorias = conteoCategorias.entrySet().stream()
                                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                                .limit(3)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                List<String> topMarcas = conteoMarcas.entrySet().stream()
                                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                                .limit(2)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                dto.setCategoriasMasCompradas(topCategorias);
                dto.setMarcasPreferidas(topMarcas);
                return dto;
        }

        private ProductCardResponse mapToProductCardResponse(Product p, Set<String> tallasEnCarrito) {
                ProductCardResponse card = new ProductCardResponse();
                card.setId(p.getId());
                card.setTitle(p.getTitle());
                card.setMarca(p.getMarca());
                card.setPrice(p.getPrice());
                card.setDiscountedPrice(p.getDiscountedPrice());
                card.setDiscountPercent(p.getDiscountPercent());
                card.setNuevo(p.isNuevo());
                if (p.getCategoria() != null) {
                        card.setCategoryName(p.getCategoria().getName());
                }

                List<String> colores = new java.util.ArrayList<>();
                List<String> tallas = new java.util.ArrayList<>();
                String mainImage = "assets/placeholder.png";
                int stockReal = 0;

                List<String> tallasEnCarritoCoincidentes = new java.util.ArrayList<>();

                if (p.getVariantes() != null && !p.getVariantes().isEmpty()) {
                        for (var v : p.getVariantes()) {
                                // SOLO variantes con stock positivo
                                if (v == null || v.getStock() <= 0) continue;

                                if (v.getColor() != null && v.getColor().getNombre() != null) {
                                        String colorNombre = v.getColor().getNombre();
                                        if (!colores.contains(colorNombre)) colores.add(colorNombre);
                                }
                                if (v.getTalla() != null && v.getTalla().getValor() != null) {
                                        String tallaValor = v.getTalla().getValor();
                                        if (!tallas.contains(tallaValor)) tallas.add(tallaValor);
                                        
                                        // Buscar si coincide con alguna del carrito
                                        for (String tc : tallasEnCarrito) {
                                                if (tallaValor.equalsIgnoreCase(tc) && !tallasEnCarritoCoincidentes.contains(tc)) {
                                                        tallasEnCarritoCoincidentes.add(tc);
                                                }
                                        }
                                }
                        }
                }

                String tallaSugerida = null;
                if (!tallasEnCarritoCoincidentes.isEmpty()) {
                        // Prioriza al azar cualquiera de las tallas coincidentes
                        java.util.Collections.shuffle(tallasEnCarritoCoincidentes);
                        tallaSugerida = tallasEnCarritoCoincidentes.get(0);
                }

                if (p.getVariantes() != null && !p.getVariantes().isEmpty()) {
                        List<com.proyecto.model.ProductVariant> variantesConStock = p.getVariantes().stream()
                                        .filter(v -> v != null && v.getStock() > 0)
                                        .collect(Collectors.toList());

                        if (!variantesConStock.isEmpty()) {
                                List<com.proyecto.model.ProductVariant> variantesIdeales = variantesConStock;
                                
                                // Si tenemos una talla sugerida, filtrar para sacar imagen/color de esa talla
                                if (tallaSugerida != null) {
                                        final String ts = tallaSugerida;
                                        List<com.proyecto.model.ProductVariant> coincidentes = variantesConStock.stream()
                                                        .filter(v -> v.getTalla() != null && v.getTalla().getValor().equalsIgnoreCase(ts))
                                                        .collect(Collectors.toList());
                                        if (!coincidentes.isEmpty()) {
                                                variantesIdeales = coincidentes;
                                        }
                                }

                                // Seleccionar variante aleatoria (así varía de color en vez de ser siempre negro)
                                java.util.Collections.shuffle(variantesIdeales);
                                var varianteElegida = variantesIdeales.get(0);

                                if (varianteElegida.getImageUrls() != null && !varianteElegida.getImageUrls().isEmpty()) {
                                        mainImage = varianteElegida.getImageUrls().get(0);
                                }
                                stockReal = varianteElegida.getStock();
                        }
                }

                card.setStock(stockReal);
                card.setAvailableColors(colores);
                card.setAvailableSizes(tallas);
                card.setSuggestedSize(tallaSugerida);
                card.setMainImageUrl(mainImage);
                return card;
        }
}