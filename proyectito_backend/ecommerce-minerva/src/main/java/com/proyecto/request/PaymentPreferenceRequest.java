package com.proyecto.request;

/**
 * DTO para recibir la solicitud de creación de preferencia de pago de Mercado Pago.
 * El frontend envía el orderId del pedido ya creado en el backend,
 * y el backend calcula el monto total desde la orden real.
 */
public class PaymentPreferenceRequest {

    private Long orderId;

    public PaymentPreferenceRequest() {}

    public PaymentPreferenceRequest(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
