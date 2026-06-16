package com.proyecto.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.UserException;
import com.proyecto.model.Cart;
import com.proyecto.model.Usuario;
import com.proyecto.response.ApiResponse;
import com.proyecto.response.ProductCardResponse;
import com.proyecto.service.CartService;
import com.proyecto.service.RecomendacionService;
import com.proyecto.service.UserService;

@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;
    private final CartService cartService;
    private final UserService userService;

    public RecomendacionController(RecomendacionService recomendacionService, CartService cartService, UserService userService) {
        this.recomendacionService = recomendacionService;
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("/carrito")
    public ResponseEntity<ApiResponse<List<ProductCardResponse>>> obtenerRecomendacionesCarrito(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false) List<String> ignoredCategories) throws UserException {
        
        Usuario usuario = userService.findUserProfileByJwt(jwt);
        Cart cart = cartService.findUserCart(usuario.getId());
        
        List<ProductCardResponse> recomendaciones = recomendacionService.obtenerSugerenciasCarrito(
                cart.getCartitems().stream().toList(), usuario, ignoredCategories);
                
        return ResponseEntity.ok(new ApiResponse<>("Recomendaciones obtenidas exitosamente", true, recomendaciones));
    }
}
