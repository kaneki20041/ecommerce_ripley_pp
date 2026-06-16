package com.proyecto.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.proyecto.Exception.OrderException;
import com.proyecto.model.Order;
import com.proyecto.model.OrderItem;
import com.proyecto.repositories.OrderRepository;
import com.proyecto.response.PaymentPreferenceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de pagos de Mercado Pago.
 *
 * FLUJO DE CREACIÓN DE PREFERENCIA:
 * 1. Se busca la orden existente en la base de datos por su ID.
 * 2. Se construye la lista de items de la preferencia desde los OrderItems de la orden.
 * 3. Se configuran las back_urls (URLs de retorno para éxito, fallo y pendiente).
 * 4. Se llama al SDK de MP para crear la preferencia en la nube.
 * 5. Se retorna el preferenceId para que el frontend lo use en el Payment Brick.
 */
@Service
public class PaymentServiceImplementation implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImplementation.class);

    @Autowired
    private OrderRepository orderRepository;

    @Value("${mercadopago.frontend.url}")
    private String frontendUrl;

    /**
     * Crea una preferencia de pago de Mercado Pago para la orden indicada.
     * El preferenceId resultante es usado por el Payment Brick en el frontend.
     */
    @Override
    public PaymentPreferenceResponse createPreference(Long orderId) throws Exception {
        // 1. Buscar la orden en la base de datos
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Orden no encontrada con ID: " + orderId));

        logger.info("[MP] Creando preferencia para orderId={} | totalPrice={} | totalDiscountedPrice={} | items={}",
                orderId, order.getTotalPrice(), order.getTotalDiscountedPrice(),
                order.getOrderItems() != null ? order.getOrderItems().size() : 0);

        // 2. Construir los items de la preferencia desde los OrderItems de la orden
        List<PreferenceItemRequest> items = new ArrayList<>();

        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                // Calcular precio unitario: discountedPrice / qty, o price directamente
                int rawDiscounted = item.getDiscountedPrice() != null ? item.getDiscountedPrice() : 0;
                int rawPrice = item.getPrice() != null ? item.getPrice() : 0;
                int qty = item.getQuantity() > 0 ? item.getQuantity() : 1;

                // discountedPrice en la BD almacena el total del item (precio * qty)
                // Lo dividimos entre la cantidad para obtener el precio UNITARIO
                int unitPrice;
                if (rawDiscounted > 0) {
                    unitPrice = rawDiscounted / qty;
                } else if (rawPrice > 0) {
                    unitPrice = rawPrice / qty;
                } else {
                    unitPrice = 1; // MP no acepta 0
                }
                if (unitPrice <= 0) unitPrice = 1;

                String productTitle = (item.getProduct() != null && item.getProduct().getTitle() != null)
                        ? item.getProduct().getTitle()
                        : "Producto Ripley";

                // Limitar el título a 256 caracteres (límite de MP)
                if (productTitle.length() > 256) {
                    productTitle = productTitle.substring(0, 253) + "...";
                }

                logger.info("[MP] Item: title='{}' | qty={} | unitPrice={} (PEN)", productTitle, qty, unitPrice);

                PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                        .id(String.valueOf(item.getId()))
                        .title(productTitle)
                        .quantity(qty)
                        .unitPrice(BigDecimal.valueOf(unitPrice))
                        .currencyId("PEN") // Soles peruanos — código oficial ISO 4217
                        .build();

                items.add(itemRequest);
            }
        }

        // Si la orden no tiene items, agregar un ítem genérico con el total
        if (items.isEmpty()) {
            long totalAmount = (order.getTotalDiscountedPrice() != null && order.getTotalDiscountedPrice() > 0)
                    ? order.getTotalDiscountedPrice()
                    : (long) order.getTotalPrice();
            if (totalAmount <= 0) totalAmount = 1;

            logger.warn("[MP] La orden {} no tiene items. Usando ítem genérico con total={}", orderId, totalAmount);

            items.add(PreferenceItemRequest.builder()
                    .id("orden-" + orderId)
                    .title("Compra Ripley #" + order.getOrderId())
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(totalAmount))
                    .currencyId("PEN")
                    .build());
        }

        // 3. Configurar las back_urls
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(frontendUrl + "/checkout/success?orderId=" + orderId)
                .failure(frontendUrl + "/checkout/failure?orderId=" + orderId)
                .pending(frontendUrl + "/checkout?orderId=" + orderId)
                .build();

        // 4. Configurar el pagador
        // REGLA CRÍTICA DE MP: el email del comprador (payer) NO puede ser igual
        // al email del vendedor (collector = tu cuenta MP = gato_09@outlook.es).
        // Si son iguales, MP rechaza con error "payer_email_same_as_collector".
        // Solución: si el email coincide, usar el email del usuario de prueba de MP.

        // Email de la cuenta vendedora (tu Access Token pertenece a este email)
        final String COLLECTOR_EMAIL = "gato_09@outlook.es";
        // Email de la cuenta compradora de prueba OFICIAL creada en MP para tests
        final String TEST_BUYER_EMAIL = "test_user_1541210220588340381@testuser.com";

        String payerEmail = TEST_BUYER_EMAIL;
        String payerName = "Test";
        String payerSurname = "Comprador";

        if (order.getUser() != null && order.getUser().getEmail() != null) {
            String userEmail = order.getUser().getEmail().trim().toLowerCase();
            String collectorEmailLower = COLLECTOR_EMAIL.trim().toLowerCase();

            if (!userEmail.equals(collectorEmailLower) && userEmail.contains("@") && userEmail.contains(".")) {
                // Email diferente al del vendedor — OK para usar
                payerEmail = order.getUser().getEmail();
                payerName = order.getUser().getFirstName() != null ? order.getUser().getFirstName() : "Cliente";
                payerSurname = order.getUser().getLastName() != null ? order.getUser().getLastName() : "Ripley";
                logger.info("[MP] Usando email del usuario: {}", payerEmail);
            } else if (userEmail.equals(collectorEmailLower)) {
                // MISMO email que el vendedor → usar el de prueba
                logger.warn("[MP] ⚠️ Email del usuario '{}' es igual al del vendedor. Usando email de prueba para el payer.", userEmail);
            }
        }

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name(payerName)
                .surname(payerSurname)
                .email(payerEmail)
                .build();

        logger.info("[MP] Payer configurado: name='{}' | email='{}'", payerName, payerEmail);

        // NOTA: auto_return = "approved" solo funciona con checkout redirect (no con Bricks)
        // Con Payment Brick el retorno se maneja via back_urls del lado del Brick.
        // Lo dejamos configurado como fallback por si el usuario usa el init_point directamente.
        
        logger.info("[MP] Back URLs configurados: success='{}', failure='{}', pending='{}'", 
            backUrls.getSuccess(), backUrls.getFailure(), backUrls.getPending());
            
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .externalReference(String.valueOf(orderId))
                .payer(payer)
                // .autoReturn("approved") // LO COMENTAMOS porque da error en la validación si la URL se considera inválida por MP
                .build();

        // 6. Llamar al SDK de MP para crear la preferencia en la nube
        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            logger.info("[MP] ✅ Preferencia creada. ID={} | initPoint={}", preference.getId(), preference.getInitPoint());

            return new PaymentPreferenceResponse(
                    preference.getId(),
                    preference.getInitPoint(),
                    preference.getSandboxInitPoint()
            );

        } catch (MPApiException e) {
            // MPApiException tiene el body completo de la respuesta de MP con el error detallado
            logger.error("[MP] ❌ Error de la API de Mercado Pago:");
            logger.error("     HTTP Status: {}", e.getStatusCode());
            logger.error("     Error Code: {}", e.getApiResponse() != null ? e.getApiResponse().getContent() : "sin contenido");
            logger.error("     Mensaje: {}", e.getMessage());
            // Re-lanzar con el mensaje detallado para que el controller lo muestre
            throw new RuntimeException("Error MP (HTTP " + e.getStatusCode() + "): " +
                    (e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage()));
        }
    }

    /**
     * Procesa la notificación IPN/webhook de Mercado Pago.
     * Cuando MP confirma un pago, actualizamos el estado de la orden en la BD.
     */
    @Override
    public void processWebhookNotification(String paymentId) throws Exception {
        logger.info("[MP Webhook] Recibida notificación de pago. payment_id: {}", paymentId);

        try {
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(Long.parseLong(paymentId));

            String status = payment.getStatus();
            String externalReference = payment.getExternalReference();

            logger.info("[MP Webhook] Pago ID: {} | Estado: {} | ExternalRef (orderId): {}",
                    paymentId, status, externalReference);

            if ("approved".equals(status) && externalReference != null) {
                try {
                    Long orderId = Long.parseLong(externalReference);
                    Order order = orderRepository.findById(orderId).orElse(null);

                    if (order != null) {
                        order.setOrderStatus("PAGADO");
                        if (order.getPaymentDetails() != null) {
                            order.getPaymentDetails().setPaymentid("MP-" + paymentId);
                            order.getPaymentDetails().setStatus("COMPLETADO");
                            order.getPaymentDetails().setPaymentMethod(
                                    payment.getPaymentMethodId() != null
                                            ? payment.getPaymentMethodId().toUpperCase()
                                            : "MERCADO_PAGO"
                            );
                        }
                        orderRepository.save(order);
                        logger.info("[MP Webhook] ✅ Orden ID: {} actualizada a estado PAGADO.", orderId);
                    } else {
                        logger.warn("[MP Webhook] ⚠️ No se encontró la orden con ID: {}", orderId);
                    }
                } catch (NumberFormatException e) {
                    logger.error("[MP Webhook] ❌ ExternalReference '{}' no es válido.", externalReference);
                }
            } else {
                logger.info("[MP Webhook] Pago estado '{}' — sin actualización de orden.", status);
            }

        } catch (MPApiException e) {
            logger.error("[MP Webhook] ❌ Error consultando pago {}: HTTP {} | {}", paymentId,
                    e.getStatusCode(), e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage());
            throw e;
        }
    }

    @Override
    public String processPayment(com.proyecto.request.PaymentProcessRequest request) throws Exception {
        logger.info("[MP] Procesando pago directo para orderId={}", request.getOrderId());

        try {
            String payerEmail = request.getPayer().getEmail();
            final String COLLECTOR_EMAIL = "gato_09@outlook.es";

            if (payerEmail != null && payerEmail.trim().equalsIgnoreCase(COLLECTOR_EMAIL)) {
                logger.warn("[MP] ⚠️ Email del usuario '{}' es igual al del vendedor. Se recomienda usar un email distinto en el Brick.", payerEmail);
            }

            com.mercadopago.client.payment.PaymentPayerRequest payer = com.mercadopago.client.payment.PaymentPayerRequest.builder()
                    .email(payerEmail)
                    .build();

            if (request.getPayer().getIdentification() != null && 
                request.getPayer().getIdentification().getType() != null &&
                request.getPayer().getIdentification().getNumber() != null &&
                !request.getPayer().getIdentification().getNumber().trim().isEmpty()) {
                payer = com.mercadopago.client.payment.PaymentPayerRequest.builder()
                        .email(payerEmail)
                        .identification(com.mercadopago.client.common.IdentificationRequest.builder()
                                .type(request.getPayer().getIdentification().getType())
                                .number(request.getPayer().getIdentification().getNumber())
                                .build())
                        .build();
            }

            // Asegurar precisión de 2 decimales para evitar error 500
            BigDecimal amount = new BigDecimal(String.valueOf(request.getTransactionAmount()))
                                    .setScale(2, java.math.RoundingMode.HALF_UP);

            String issuerId = request.getIssuerId();
            if (issuerId != null && (issuerId.trim().isEmpty() || issuerId.trim().equalsIgnoreCase("null"))) {
                issuerId = null;
            }

            com.mercadopago.client.payment.PaymentCreateRequest paymentCreateRequest = com.mercadopago.client.payment.PaymentCreateRequest.builder()
                    .transactionAmount(amount)
                    .token(request.getToken())
                    .description("Compra en Ripley - Orden #" + request.getOrderId())
                    .installments(request.getInstallments() != null ? request.getInstallments() : 1)
                    .paymentMethodId(request.getPaymentMethodId())
                    .issuerId(issuerId)
                    .payer(payer)
                    .externalReference(String.valueOf(request.getOrderId()))
                    .build();

            // Evitar error 500 por Idempotency Key faltante o repetida
            com.mercadopago.core.MPRequestOptions requestOptions = com.mercadopago.core.MPRequestOptions.builder()
                    .customHeaders(java.util.Collections.singletonMap("x-idempotency-key", java.util.UUID.randomUUID().toString()))
                    .build();

            // LOG the payload for debugging if 500 persists
            logger.info("[MP] Payload a enviar: amount={}, token={}, method={}, issuer={}, installments={}, email={}", 
                    amount, request.getToken() != null ? "***" : "null", request.getPaymentMethodId(), issuerId, 
                    request.getInstallments(), payerEmail);

            PaymentClient client = new PaymentClient();
            Payment payment = client.create(paymentCreateRequest, requestOptions);

            logger.info("[MP] ✅ Pago procesado correctamente. ID={}, Status={}", payment.getId(), payment.getStatus());
            
            // Actualizamos la orden
            Order order = orderRepository.findById(request.getOrderId()).orElse(null);
            if (order != null) {
                if ("approved".equals(payment.getStatus())) {
                    order.setOrderStatus("PAGADO");
                }
                if (order.getPaymentDetails() != null) {
                    order.getPaymentDetails().setPaymentid("MP-" + payment.getId());
                    order.getPaymentDetails().setStatus(payment.getStatus());
                    order.getPaymentDetails().setPaymentMethod(
                            payment.getPaymentMethodId() != null
                                    ? payment.getPaymentMethodId().toUpperCase()
                                    : "MERCADO_PAGO"
                    );
                }
                orderRepository.save(order);
            }

            return String.valueOf(payment.getId());

        } catch (MPApiException e) {
            logger.error("[MP] ❌ Error procesando el pago (Brick): HTTP {}", e.getStatusCode());
            logger.error("     Detalle: {}", e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage());
            throw new RuntimeException("Error MP (HTTP " + e.getStatusCode() + "): " +
                    (e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage()));
        }
    }
}
