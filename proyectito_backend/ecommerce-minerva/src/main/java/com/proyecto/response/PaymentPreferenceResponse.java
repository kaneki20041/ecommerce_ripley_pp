package com.proyecto.response;

/**
 * DTO de respuesta al crear una preferencia de Mercado Pago.
 * El frontend usa el preferenceId para renderizar el Payment Brick.
 * initPoint y sandboxInitPoint son URLs alternativas por si se quiere
 * redirigir al usuario (modo clásico), aunque con Bricks no es necesario.
 */
public class PaymentPreferenceResponse {

    private String preferenceId;
    private String initPoint;
    private String sandboxInitPoint;

    public PaymentPreferenceResponse() {}

    public PaymentPreferenceResponse(String preferenceId, String initPoint, String sandboxInitPoint) {
        this.preferenceId = preferenceId;
        this.initPoint = initPoint;
        this.sandboxInitPoint = sandboxInitPoint;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }

    public String getInitPoint() {
        return initPoint;
    }

    public void setInitPoint(String initPoint) {
        this.initPoint = initPoint;
    }

    public String getSandboxInitPoint() {
        return sandboxInitPoint;
    }

    public void setSandboxInitPoint(String sandboxInitPoint) {
        this.sandboxInitPoint = sandboxInitPoint;
    }
}
