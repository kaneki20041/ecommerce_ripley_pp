package com.proyecto.service;

import com.proyecto.request.PaymentPreferenceRequest;
import com.proyecto.response.PaymentPreferenceResponse;

/**
 * Interface del servicio de pagos de Mercado Pago.
 * Separa la lógica de negocio del controlador HTTP.
 */
public interface PaymentService {

    /**
     * Crea una preferencia de pago en Mercado Pago para una orden existente.
     * Busca la orden, construye los items y retorna el preferenceId para el Brick.
     * @param orderId ID de la orden ya creada en el backend
     * @return PaymentPreferenceResponse con preferenceId y URLs de MP
     */
    PaymentPreferenceResponse createPreference(Long orderId) throws Exception;

    /**
     * Procesa la notificación IPN/webhook de Mercado Pago.
     * Consulta el estado real del pago y actualiza la orden correspondiente.
     * @param paymentId ID del pago retornado por MP
     */
    void processWebhookNotification(String paymentId) throws Exception;

    /**
     * Procesa el pago directamente usando los datos recolectados por Payment Brick.
     * @param request Datos del pago tokenizado desde el frontend
     * @return String paymentId o status
     */
    String processPayment(com.proyecto.request.PaymentProcessRequest request) throws Exception;
}
