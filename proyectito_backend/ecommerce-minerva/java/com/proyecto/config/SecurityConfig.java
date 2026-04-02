package com.proyecto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.proyecto.security.JwtValidationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtValidationFilter jwtValidationFilter;

    public SecurityConfig(JwtValidationFilter jwtValidationFilter) {
        this.jwtValidationFilter = jwtValidationFilter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                    // 1. PÚBLICOS
                    .requestMatchers(
                        "/auth/**",
                        "/api/public/**",
                        "/api/products/public/**",
                        "/error",
                        "/actuator/health"
                    ).permitAll()

                    // 2. COMPARTIDO (ADMIN Y EMPLEADO) - ¡Va antes de la regla general de admin!
                    .requestMatchers(
                        "/api/admin/products/**"
                    ).hasAnyRole("ADMIN", "EMPLOYEE")

                    // 3. EXCLUSIVO DE ADMIN (Todo el resto de /api/admin/)
                    .requestMatchers(
                        "/api/admin/**",
                        "/auth/signup/admin"
                    ).hasRole("ADMIN")

                    // 4. USUARIOS REGULARES (Y staff)
                    .requestMatchers(
                        "/api/user/**",
                        "/api/cart/**",
                        "/api/orders/**"
                    ).hasAnyRole("USER", "ADMIN", "EMPLOYEE")

                    // 5. EL RECOGEDOR FINAL (¡SIEMPRE AL ÚLTIMO!)
                    .anyRequest().authenticated()
                )

            // AGREGAR FILTRO JWT ANTES DEL FILTRO DE AUTENTICACIÓN ESTÁNDAR
            // Este filtro intercepta todas las peticiones y valida el token JWT
            .addFilterBefore(jwtValidationFilter, UsernamePasswordAuthenticationFilter.class)

            // DESACTIVAR CSRF (no es necesario con JWT stateless)
            // CSRF protege contra ataques cuando usas cookies de sesión
            // Como JWT va en header Authorization, no necesitamos CSRF
            .csrf(csrf -> csrf.disable())

            // DESACTIVAR HTTP BASIC (no lo usamos, solo JWT)
            .httpBasic(httpBasic -> httpBasic.disable())

            // DESACTIVAR FORM LOGIN (no usamos formularios, solo API REST)
            .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }

    /**
     * Bean para encriptar contraseñas con BCrypt
     *
     * BCrypt es un algoritmo de hash seguro que:
     * - Incluye "salt" automático (previene rainbow tables)
     * - Es computacionalmente costoso (previene fuerza bruta)
     * - Es adaptativo (puede aumentar la dificultad con el tiempo)
     *
     * NUNCA almacenes contraseñas en texto plano
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean para AuthenticationManager
     *
     * Necesario si quieres usar autenticación programática
     * (aunque con JWT normalmente no lo usas directamente)
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}