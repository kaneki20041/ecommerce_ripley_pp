package com.proyecto.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Inicializador del SDK de Mercado Pago.
 * Se encarga de registrar el Access Token globalmente al arrancar la aplicación,
 * para que todos los clientes del SDK (PreferenceClient, PaymentClient) funcionen
 * correctamente sin necesidad de configurar el token en cada llamada.
 *
 * Nota: el nombre de la clase es MercadoPagoInitializer (y no MercadoPagoConfig)
 * para evitar conflicto de nombres con la clase com.mercadopago.MercadoPagoConfig del SDK.
 */
@Configuration
public class MercadoPagoInitializer {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    /**
     * Se ejecuta una sola vez al arrancar el contexto de Spring.
     * Registra el Access Token globalmente en el SDK de MP.
     */
    @PostConstruct
    public void init() {
        com.mercadopago.MercadoPagoConfig.setAccessToken(accessToken);
    }
}
