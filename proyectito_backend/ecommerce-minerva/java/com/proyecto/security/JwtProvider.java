package com.proyecto.security;

import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.proyecto.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // Crear la clave de firma a partir del secret
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generateToken(Authentication auth, Long userId) {
        // Obtener todos los roles/permisos del usuario
        String authorities = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        // Obtener el rol principal (el primero)
        String role = auth.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse("ROLE_USER");

        // Calcular fecha de expiración
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        // Construir y firmar el token
        String token = Jwts.builder()
                .issuer(jwtProperties.getIssuer())              // Quién emitió el token
                .subject(userId != null ? userId.toString() : null) // ID del usuario (claim estándar)
                .issuedAt(now)                                   // Cuándo se emitió
                .expiration(expiryDate)                          // Cuándo expira
                .claim("email", auth.getName())                  // Email del usuario
                .claim("authorities", authorities)               // Todos los permisos
                .claim("role", role)                            // Rol principal
                .signWith(key)                                  // Firmar con la clave secreta
                .compact();                                     // Generar el string final

        logger.debug("Token JWT generado para usuario: {}", auth.getName());
        return token;
    }

    /**
     * Sobrecarga del método para retrocompatibilidad
     */
    public String generateToken(Authentication auth) {
        return generateToken(auth, null);
    }

    /**
     * Extrae el email del usuario desde un token JWT
     *
     * @param jwt - Token JWT (puede venir con "Bearer " o sin él)
     * @return Email del usuario
     * @throws JwtException si el token es inválido o ha expirado
     */
    public String getEmailFromToken(String jwt) {
        // Limpiar el token si viene con "Bearer "
        jwt = cleanToken(jwt);

        try {
            Claims claims = parseToken(jwt);
            return String.valueOf(claims.get("email"));
        } catch (ExpiredJwtException e) {
            logger.warn("Token expirado para email: {}", e.getClaims().get("email"));
            throw new JwtException("Token expirado", e);
        } catch (Exception e) {
            logger.error("Error al extraer email del token", e);
            throw new JwtException("Token inválido", e);
        }
    }


    public String getRoleFromToken(String jwt) {
        jwt = cleanToken(jwt);

        try {
            Claims claims = parseToken(jwt);
            return String.valueOf(claims.get("role"));
        } catch (Exception e) {
            logger.error("Error al extraer rol del token", e);
            throw new JwtException("Token inválido", e);
        }
    }

    /**
     * Extrae todas las authorities del token
     *
     * @param jwt - Token JWT
     * @return String con authorities separadas por coma
     * @throws JwtException si el token es inválido
     */
    public String getAuthoritiesFromToken(String jwt) {
        jwt = cleanToken(jwt);

        try {
            Claims claims = parseToken(jwt);
            return String.valueOf(claims.get("authorities"));
        } catch (Exception e) {
            logger.error("Error al extraer authorities del token", e);
            throw new JwtException("Token inválido", e);
        }
    }

    /**
     * Valida si un token JWT es válido
     *
     * Un token es válido si:
     * - La firma es correcta
     * - No ha expirado
     * - Tiene el formato correcto
     *
     * @param jwt - Token JWT
     * @return true si es válido, false si no
     */
    public boolean validateToken(String jwt) {
        jwt = cleanToken(jwt);

        try {
            parseToken(jwt);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expirado");
            return false;
        } catch (JwtException e) {
            logger.warn("Token inválido: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error validando token", e);
            return false;
        }
    }

    /**
     * Extrae la fecha de expiración de un token
     *
     * @param jwt - Token JWT
     * @return Fecha de expiración
     */
    public Date getExpirationDate(String jwt) {
        jwt = cleanToken(jwt);
        Claims claims = parseToken(jwt);
        return claims.getExpiration();
    }

    /**
     * Verifica si un token ha expirado
     *
     * @param jwt - Token JWT
     * @return true si ha expirado, false si aún es válido
     */
    public boolean isTokenExpired(String jwt) {
        try {
            Date expiration = getExpirationDate(jwt);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // ============================================
    // MÉTODOS PRIVADOS HELPER
    // ============================================

    /**
     * Parsea un token JWT y extrae sus claims
     *
     * @param jwt - Token JWT limpio (sin "Bearer ")
     * @return Claims del token
     * @throws JwtException si el token es inválido
     */
    private Claims parseToken(String jwt) {
        return Jwts.parser()
            .verifyWith(key)                    // Verificar con la clave secreta
            .build()
            .parseSignedClaims(jwt)             // Parsear y validar firma
            .getPayload();                       // Obtener el payload (claims)
    }

    /**
     * Limpia el token removiendo el prefijo "Bearer " si existe
     *
     * @param jwt - Token JWT que puede tener "Bearer " al inicio
     * @return Token limpio sin "Bearer "
     */
    private String cleanToken(String jwt) {
        if (jwt != null && jwt.startsWith(jwtProperties.getTokenPrefix())) {
            return jwt.substring(jwtProperties.getTokenPrefix().length());
        }
        return jwt;
    }
}