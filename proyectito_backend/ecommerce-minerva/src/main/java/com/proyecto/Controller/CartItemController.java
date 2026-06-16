package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.CartItemException;
import com.proyecto.Exception.UserException;
import com.proyecto.model.CartItem;
import com.proyecto.model.Usuario;
import com.proyecto.response.ApiResponse;
import com.proyecto.service.CartItemService;
import com.proyecto.service.UserService;

@RestController
@RequestMapping("/api/cart_item")
public class CartItemController {

	@Autowired
	private CartItemService cartItemService;

	@Autowired
	private UserService userService;

	@DeleteMapping("/{cartItemId}")
    @ManagedOperation(description = "Remove Cart Item From Cart")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @PathVariable Long cartItemId,
            @RequestHeader("Authorization") String jwt) throws UserException, CartItemException {

        Usuario user = userService.findUserProfileByJwt(jwt);
        cartItemService.removerCartItem(user.getId(), cartItemId);

        // Usamos tu ApiResponse sin datos, ideal para un DELETE
        ApiResponse<Void> res = new ApiResponse<>("Item deleted from cart", true);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

	@PutMapping("/{cartItemId}")
	@ManagedOperation(description = "Update Cart Item Quantity")
	public ResponseEntity<ApiResponse<CartItem>> updateCartItem(
			@RequestBody CartItem cartItem, // Recibimos la nueva cantidad desde el frontend
			@PathVariable Long cartItemId,
			@RequestHeader("Authorization") String jwt) throws UserException, CartItemException {

		// 1. Manejo del JWT en el controlador
		Usuario user = userService.findUserProfileByJwt(jwt);

		// 2. Ejecución de la lógica con IDs limpios
		CartItem updatedItem = cartItemService.updateCartItem(user.getId(), cartItemId, cartItem);

		// 3. Respuesta estándar con tu formato ApiResponse
		ApiResponse<CartItem> res = new ApiResponse<>("Item updated successfully", true, updatedItem);

		return new ResponseEntity<>(res, HttpStatus.OK);
	}
}
