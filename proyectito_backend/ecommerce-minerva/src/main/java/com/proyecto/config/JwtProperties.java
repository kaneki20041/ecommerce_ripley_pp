package com.proyecto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para JWT
 *
 * Esta clase lee la configuración de JWT desde application.properties
 * en lugar de tener valores hardcodeados en el código.
 *
 * VENTAJAS:
 * - Valores sensibles NO están en el código fuente
 * - Fácil cambiar configuración por ambiente (dev, test, prod)
 * - Permite usar variables de entorno en producción
 * - Más seguro y profesional
 *
 * En application.properties:
 * jwt.secret=tu-clave-secreta-minimo-256-bits
 * jwt.expiration=86400000
 * jwt.header=Authorization
 *
 * En producción, usa variables de entorno:
 * jwt.secret=${JWT_SECRET:default-secret-only-for-dev}
 *
 * @author Tu Nombre
 * @version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Clave secreta para firmar y verificar tokens JWT
     *
     * REQUISITOS:
     * - Mínimo 256 bits (32 caracteres)
     * - Aleatoria y única
     * - NUNCA compartir ni subir a Git
     * - Rotar periódicamente en producción
     *
     * En application.properties:
     * jwt.secret=${JWT_SECRET:default-secret-key-change-me-in-production}
     *
     * En producción (variable de entorno):
     * export JWT_SECRET="clave-super-segura-256-bits-minimo"
     */
    private String secret;

    /**
     * Tiempo de expiración del token en milisegundos
     *
     * RECOMENDACIONES:
     * - Desarrollo: 86400000 (24 horas)
     * - Producción: 3600000 (1 hora) + Refresh Token
     * - APIs públicas: 900000 (15 minutos)
     *
     * En application.properties:
     * jwt.expiration=86400000
     *
     * CONVERSIÓN:
     * 15 min  = 900000
     * 1 hora  = 3600000
     * 24 hrs  = 86400000
     * 7 días  = 604800000
     */
    private long expiration = 86400000; // Default: 24 horas

    /**
     * Nombre del header HTTP donde se envía el token
     *
     * Estándar: "Authorization"
     * El token se envía como: "Bearer <token>"
     *
     * En application.properties:
     * jwt.header=Authorization
     */
    private String header = "Authorization";

    /**
     * Prefijo del token en el header
     *
     * Estándar: "Bearer "
     * Ejemplo completo: "Bearer eyJhbGciOiJIUzI1NiJ9..."
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Emisor del token (issuer)
     *
     * Identifica quién generó el token
     * Útil si tienes múltiples servicios
     *
     * En application.properties:
     * jwt.issuer=tienda-textil-api
     */
    private String issuer = "tienda-textil-api";

    // ============================================
    // GETTERS Y SETTERS
    // ============================================

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Método helper para validar que la configuración es correcta
     * Se ejecuta al iniciar la aplicación
     */
    public void validate() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret debe tener al menos 256 bits (32 caracteres). " +
                "Actual: " + (secret != null ? secret.length() : 0) + " caracteres"
            );
        }

        if (expiration <= 0) {
            throw new IllegalStateException(
                "JWT expiration debe ser mayor a 0. Actual: " + expiration
            );
        }
    }
}