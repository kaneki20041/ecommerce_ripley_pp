package com.proyecto.Controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.model.Cart;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.UserRepository;
import com.proyecto.request.LoginRequest;
import com.proyecto.request.RegisterRequest;
import com.proyecto.response.AuthResponse;
import com.proyecto.security.JwtProvider;
import com.proyecto.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final CartService cartService;

    public AuthController(UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         JwtProvider jwtProvider,
                         CartService cartService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.cartService = cartService;
    }

    /**
     * Registro de usuario normal (ROLE_USER)
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        logger.info("Iniciando registro de usuario: {}", request.getEmail());

        try {
            // Verificar si el email ya existe
            if (userRepository.findByEmail(request.getEmail()) != null) {
                logger.warn("Email ya registrado: {}", request.getEmail());
                return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new AuthResponse("El email ya está en uso"));
            }

            // Crear nuevo usuario
            Usuario newUser = new Usuario();
            newUser.setFirstName(request.getFirstName());
            newUser.setLastName(request.getLastName());
            newUser.setEmail(request.getEmail());
            newUser.setCelular(request.getCelular());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setRole(Usuario.UserRole.USER);
            newUser.setCreatedAt(LocalDateTime.now());

            // Guardar usuario
            Usuario savedUser = userRepository.save(newUser);

            // Crear carrito para el usuario
            Cart cart = cartService.createCart(savedUser);

            logger.info("Usuario registrado exitosamente: {} (ID: {})",
                       savedUser.getEmail(), savedUser.getId());

            // Generar token
            Authentication authentication = createAuthentication(savedUser);
            String token = jwtProvider.generateToken(authentication, savedUser.getId());

            // Construir respuesta
            return ResponseEntity.status(HttpStatus.CREATED)
            		.body(new AuthResponse(token,"Registro exitoso"));

        } catch (Exception e) {
            logger.error("Error durante el registro: {}", request.getEmail(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse("Error durante el registro"));
        }
    }

    /**
     * Registro de administrador (ROLE_ADMIN)
     * NOTA: Esta ruta debe estar protegida en producción


    /**
     * Login de usuario
     */
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        logger.info("Intento de login: {}", request.getEmail());

        try {
            // Buscar usuario
            Usuario user = userRepository.findByEmail(request.getEmail());
            if (user == null) {
                logger.warn("Usuario no encontrado: {}", request.getEmail());
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Credenciales inválidas"));
            }

            // Verificar contraseña
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.warn("Contraseña incorrecta para: {}", request.getEmail());
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Credenciales inválidas"));
            }

            // Generar token
            Authentication authentication = createAuthentication(user);
            String token = jwtProvider.generateToken(authentication, user.getId());

            logger.info("Login exitoso: {} (Rol: {})", user.getEmail(), user.getRole());

            // Construir respuesta
            return ResponseEntity.status(HttpStatus.CREATED)
            		.body(new AuthResponse(token,"Login Exitoso"));

        } catch (Exception e) {
            logger.error("Error durante el login: {}", request.getEmail(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse("Error durante el login"));
        }
    }

    /**
     * Crear autenticación con authorities
     */
    private Authentication createAuthentication(Usuario user) {
        String role = "ROLE_" + user.getRole().name();
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(role)
        );

        return new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
    }

}