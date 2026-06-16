package com.proyecto.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.proyecto.Exception.ProductException;
import com.proyecto.Exception.UserException;
import com.proyecto.model.Favorite;
import com.proyecto.model.Product;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.FavoriteRepository;
import com.proyecto.repositories.UserRepository;
import com.proyecto.response.ApiResponse;
import com.proyecto.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public FavoriteController(FavoriteRepository favoriteRepository, UserRepository userRepository, ProductService productService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    private Usuario getAuthenticatedUser() throws UserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UserException("No autenticado");
        }
        Usuario user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new UserException("Usuario no encontrado");
        }
        return user;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Favorite>>> getUserFavorites() throws UserException {
        Usuario user = getAuthenticatedUser();
        List<Favorite> favorites = favoriteRepository.findByUserOrderByAddedAtDesc(user);
        return new ResponseEntity<>(new ApiResponse<>("Favoritos obtenidos", true, favorites), HttpStatus.OK);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Favorite>> addFavorite(@PathVariable Long productId) throws UserException, ProductException {
        Usuario user = getAuthenticatedUser();
        Product product = productService.findProductById(productId);

        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            return new ResponseEntity<>(new ApiResponse<>("El producto ya está en favoritos", false, null), HttpStatus.BAD_REQUEST);
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        Favorite saved = favoriteRepository.save(favorite);

        return new ResponseEntity<>(new ApiResponse<>("Producto agregado a favoritos", true, saved), HttpStatus.CREATED);
    }

    @DeleteMapping("/{productId}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable Long productId) throws UserException, ProductException {
        Usuario user = getAuthenticatedUser();
        Product product = productService.findProductById(productId);

        favoriteRepository.deleteByUserAndProduct(user, product);
        
        return new ResponseEntity<>(new ApiResponse<>("Producto eliminado de favoritos", true, null), HttpStatus.OK);
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(@PathVariable Long productId) {
        try {
            Usuario user = getAuthenticatedUser();
            Product product = productService.findProductById(productId);
            boolean exists = favoriteRepository.existsByUserAndProduct(user, product);
            return new ResponseEntity<>(new ApiResponse<>("Verificación exitosa", true, exists), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>("Error al verificar", false, false), HttpStatus.OK);
        }
    }
}
