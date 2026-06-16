package com.proyecto.Controller;

import com.proyecto.response.ApiResponse;
import com.proyecto.request.PaymentPreferenceRequest;
import com.proyecto.response.PaymentPreferenceResponse;
import com.proyecto.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de pagos con Mercado Pago.
 *
 * ENDPOINTS:
 * - POST /api/payments/create-preference
 *   Recibe el orderId, crea la preferencia en MP y retorna el preferenceId para el Payment Brick.
 *
 * - POST /api/payments/webhook
 *   Recibe notificaciones IPN de Mercado Pago y actualiza el estado del pedido.
 *   No requiere JWT (MP lo llama directamente, sin token de usuario).
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * Endpoint 1: Crear Preferencia de Pago
     *
     * El frontend llama a este endpoint DESPUÉS de crear la orden.
     * Retorna el preferenceId que el Payment Brick necesita para renderizarse.
     *
     * Requiere JWT del usuario autenticado (filtro de seguridad lo valida automáticamente).
     */
    @PostMapping("/create-preference")
    public ResponseEntity<ApiResponse<PaymentPreferenceResponse>> createPreference(
            @RequestBody PaymentPreferenceRequest request) {
        try {
            PaymentPreferenceResponse response = paymentService.createPreference(request.getOrderId());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Preferencia de pago creada exitosamente",
                    true,
                    response
            ));
        } catch (Exception e) {
            logger.error("[MP] Error al crear preferencia de pago para orderId {}: {}",
                    request.getOrderId(), e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Error al crear la preferencia de pago: " + e.getMessage(),
                    false,
                    null
            ));
        }
    }

    /**
     * Endpoint 1.5: Procesar el pago directamente con el Payment Brick
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<String>> processPayment(
            @RequestBody com.proyecto.request.PaymentProcessRequest request) {
        try {
            String paymentId = paymentService.processPayment(request);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Pago procesado exitosamente",
                    true,
                    paymentId
            ));
        } catch (Exception e) {
            logger.error("[MP] Error procesando pago para orderId {}: {}",
                    request.getOrderId(), e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Error al procesar el pago: " + e.getMessage(),
                    false,
                    null
            ));
        }
    }

    /**
     * Endpoint 2: Webhook de Mercado Pago (IPN - Instant Payment Notification)
     *
     * Mercado Pago llama a este endpoint automáticamente cuando un pago cambia de estado.
     * NO requiere JWT (la autenticación se hace por el X-Signature header de MP).
     *
     * Para pruebas locales con ngrok, configurar la URL en el panel de MP:
     * https://tu-id.ngrok.app/api/payments/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestParam(value = "type", required = false) String type,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            logger.info("[MP Webhook] Notificación recibida. type: {} | body: {}", type, body);

            // Solo procesar notificaciones de tipo "payment"
            if ("payment".equals(type) && body != null) {
                Object dataObj = body.get("data");
                if (dataObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) dataObj;
                    Object paymentIdObj = data.get("id");
                    if (paymentIdObj != null) {
                        String paymentId = paymentIdObj.toString();
                        paymentService.processWebhookNotification(paymentId);
                    }
                }
            }
            // MP espera un HTTP 200 para confirmar recepción
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("[MP Webhook] Error procesando notificación: {}", e.getMessage());
            // Retornar 200 para que MP no reintente indefinidamente
            return ResponseEntity.ok().build();
        }
    }
}
