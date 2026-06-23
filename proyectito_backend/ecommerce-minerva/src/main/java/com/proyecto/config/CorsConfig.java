package com.proyecto.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String[] allowedOrigins;


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ============================================
        // ORÍGENES PERMITIDOS
        // ============================================
        // Lista de dominios que pueden hacer peticiones a tu API
        // En desarrollo: http://localhost:4200
        // En producción: https://tu-dominio.com
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));

        // ============================================
        // MÉTODOS HTTP PERMITIDOS
        // ============================================
        // Permite GET, POST, PUT, DELETE, PATCH, OPTIONS
        configuration.setAllowedMethods(Arrays.asList(
            "GET",      // Leer recursos
            "POST",     // Crear recursos
            "PUT",      // Actualizar recursos (completo)
            "DELETE",   // Eliminar recursos
            "PATCH",    // Actualizar recursos (parcial)
            "OPTIONS"   // Preflight requests de CORS
        ));

        // ============================================
        // HEADERS PERMITIDOS
        // ============================================
        // Headers que el cliente puede enviar
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",  // Para enviar JWT token
            "Content-Type",   // Para especificar tipo de datos (JSON, etc)
            "Accept",         // Para especificar formato de respuesta
            "X-Requested-With" // Para peticiones AJAX
        ));

        // ============================================
        // HEADERS EXPUESTOS
        // ============================================
        // Headers que el navegador puede leer en la respuesta
        // Útil si tu backend envía información en headers personalizados
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",  // Permite leer el token JWT de la respuesta
            "Content-Disposition" // Útil para descargas de archivos
        ));

        // ============================================
        // PERMITIR CREDENCIALES
        // ============================================
        // true = Permite enviar cookies y headers de autenticación
        // Necesario para JWT en header Authorization
        configuration.setAllowCredentials(true);

        // ============================================
        // TIEMPO DE CACHÉ (en segundos)
        // ============================================
        // El navegador cachea la configuración CORS para evitar
        // hacer peticiones OPTIONS (preflight) en cada request
        // 3600s = 1 hora
        configuration.setMaxAge(3600L);

        // Aplicar la configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}