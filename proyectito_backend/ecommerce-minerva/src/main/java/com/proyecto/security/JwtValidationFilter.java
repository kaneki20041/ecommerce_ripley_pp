package com.proyecto.security;

import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.proyecto.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que valida tokens JWT en cada petición HTTP
 *
 * Este filtro se ejecuta ANTES de que Spring Security procese la petición.
 *
 * FLUJO:
 * 1. Usuario hace petición → Filtro intercepta
 * 2. Busca header "Authorization"
 * 3. Si existe token → Valida y extrae información
 * 4. Crea autenticación en Spring Security
 * 5. Continúa con la petición
 *
 * RUTAS IGNORADAS:
 * - /auth/** (login, signup) - No necesitan token
 * - /api/public/** - APIs públicas
 * - /error - Manejo de errores
 *
 * IMPORTANTE: Este filtro NO genera errores HTTP directamente,
 * solo marca la petición como "no autenticada" y deja que
 * Spring Security maneje los errores 401 (Unauthorized)
 *
 * @author Tu Nombre
 * @version 2.0
 */
@Component // Ahora es un componente de Spring para inyección de dependencias
public class JwtValidationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidationFilter.class);

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    /**
     * Constructor con inyección de dependencias
     * 
     * @param jwtProperties - Configuración de JWT
     */
    public JwtValidationFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * Método principal del filtro que se ejecuta en cada petición
     *
     * @param request     - Petición HTTP entrante
     * @param response    - Respuesta HTTP
     * @param filterChain - Cadena de filtros de Spring
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // ============================================
        // IGNORAR RUTAS PÚBLICAS
        // ============================================
        // Estas rutas no necesitan autenticación
        if (shouldNotFilter(request)) {
            logger.debug("Ruta pública, saltando validación JWT: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // ============================================
        // OBTENER TOKEN DEL HEADER
        // ============================================
        String jwt = extractJwtFromRequest(request);

        if (jwt != null && !jwt.isEmpty()) {
            try {
                // ============================================
                // VALIDAR Y PARSEAR TOKEN
                // ============================================
                Claims claims = parseAndValidateToken(jwt);

                // ============================================
                // EXTRAER INFORMACIÓN DEL USUARIO
                // ============================================
                String email = extractEmail(claims);
                String authorities = extractAuthorities(claims);
                String role = extractRole(claims);

                // ============================================
                // CREAR AUTENTICACIÓN EN SPRING SECURITY
                // ============================================
                Authentication authentication = createAuthentication(email, authorities, role);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Usuario autenticado: {} con rol: {}", email, role);

            } catch (ExpiredJwtException e) {
                // Token expirado - el usuario debe hacer login nuevamente
                logger.warn("Token expirado en petición a: {}", path);
                // NO lanzar excepción, dejar que Spring Security maneje el 401

            } catch (JwtException e) {
                // Token inválido (firma incorrecta, formato incorrecto, etc.)
                logger.warn("Token JWT inválido en petición a: {} - {}", path, e.getMessage());
                // NO lanzar excepción, dejar que Spring Security maneje el 401

            } catch (Exception e) {
                // Cualquier otro error
                logger.error("Error procesando token JWT: ", e);
                // NO lanzar excepción, dejar que Spring Security maneje el 401
            }
        } else {
            // No hay token en el header
            logger.debug("No se encontró token JWT en la petición a: {}", path);
        }

        // ============================================
        // CONTINUAR CON LA CADENA DE FILTROS
        // ============================================
        filterChain.doFilter(request, response);
    }

    /**
     * Define qué rutas NO deben ser filtradas por este filtro
     *
     * @param request - Petición HTTP
     * @return true si la ruta debe saltarse el filtro
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/auth/") || // Rutas de autenticación
                path.startsWith("/api/public/") || // APIs públicas
                path.startsWith("/error") || // Manejo de errores
                path.startsWith("/actuator/health"); // Health check
    }

    // ============================================
    // MÉTODOS PRIVADOS HELPER
    // ============================================

    /**
     * Extrae el token JWT del header Authorization
     *
     * @param request - Petición HTTP
     * @return Token JWT sin el prefijo "Bearer ", o null si no existe
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getHeader());

        if (bearerToken != null && bearerToken.startsWith(jwtProperties.getTokenPrefix())) {
            return bearerToken.substring(jwtProperties.getTokenPrefix().length());
        }

        return null;
    }

    /**
     * Parsea y valida un token JWT
     *
     * @param jwt - Token JWT
     * @return Claims del token
     * @throws JwtException si el token es inválido o ha expirado
     */
    private Claims parseAndValidateToken(String jwt) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    /**
     * Extrae el email del usuario de los claims
     *
     * @param claims - Claims del token JWT
     * @return Email del usuario
     */
    private String extractEmail(Claims claims) {
        Object email = claims.get("email");
        return email != null ? String.valueOf(email) : "";
    }

    /**
     * Extrae las authorities del usuario de los claims
     *
     * @param claims - Claims del token JWT
     * @return Authorities como String separado por comas
     */
    private String extractAuthorities(Claims claims) {
        Object authorities = claims.get("authorities");
        return authorities != null && !authorities.equals("null")
                ? String.valueOf(authorities)
                : "";
    }

    /**
     * Extrae el rol del usuario de los claims
     *
     * @param claims - Claims del token JWT
     * @return Rol del usuario (ej: "ROLE_USER")
     */
    private String extractRole(Claims claims) {
        Object role = claims.get("role");

        if (role == null || role.equals("null")) {
            return "";
        }

        String roleStr = String.valueOf(role);

        // Asegurar que el rol tenga el prefijo ROLE_
        if (!roleStr.startsWith("ROLE_")) {
            roleStr = "ROLE_" + roleStr;
        }

        return roleStr;
    }

    /**
     * Crea un objeto Authentication de Spring Security
     *
     * @param email       - Email del usuario
     * @param authorities - Authorities del usuario
     * @param role        - Rol principal del usuario
     * @return Objeto Authentication
     */
    private Authentication createAuthentication(String email, String authorities, String role) {
        // Combinar authorities existentes con el rol
        String allAuthorities = combineAuthorities(authorities, role);

        // Convertir string de authorities a lista de GrantedAuthority
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(allAuthorities);

        // Crear y retornar el objeto de autenticación
        return new UsernamePasswordAuthenticationToken(
                email, // Principal (identificador del usuario)
                null, // Credentials (no las necesitamos en JWT)
                grantedAuthorities // Authorities (permisos)
        );
    }

    /**
     * Combina authorities y rol en un solo string
     *
     * @param authorities - Authorities existentes
     * @param role        - Rol del usuario
     * @return String con todas las authorities combinadas
     */
    private String combineAuthorities(String authorities, String role) {
        StringBuilder combined = new StringBuilder();

        if (authorities != null && !authorities.isEmpty()) {
            combined.append(authorities);
        }

        if (role != null && !role.isEmpty()) {
            if (combined.length() > 0) {
                combined.append(",");
            }
            combined.append(role);
        }

        return combined.toString();
    }
}