package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.CartItemException;
import com.proyecto.Exception.ProductException;
import com.proyecto.Exception.UserException;
import com.proyecto.model.Cart;
import com.proyecto.model.Usuario;
import com.proyecto.request.AddItemRequest;
import com.proyecto.response.ApiResponse;
import com.proyecto.service.CartService;
import com.proyecto.service.UserService;
@RestController
@RequestMapping("/api/cart")
public class CartController {
	@Autowired
	private CartService cartService;

	@Autowired
	private UserService userService;

	@GetMapping("/")
	@ManagedOperation(description = "find cart by user id")
	public ResponseEntity<Cart>findUserCart(@RequestHeader ("Authorization")String jwt) throws UserException{
		Usuario user=userService.findUserProfileByJwt(jwt);
		Cart cart=cartService.findUserCart(user.getId());
	return new ResponseEntity<>(cart,HttpStatus.OK);
	}

	@PostMapping("/add") // (Recuerda que POST suele ser mejor semánticamente para agregar)
	public ResponseEntity<ApiResponse<Cart>> additemToCart(
			@RequestBody AddItemRequest req,
			@RequestHeader("Authorization") String jwt) throws UserException, ProductException, CartItemException {

		Usuario user = userService.findUserProfileByJwt(jwt);

		cartService.addCartItem(user.getId(), req);

		Cart updatedCart = cartService.findUserCart(user.getId());

		ApiResponse<Cart> res = new ApiResponse<>(
				"Item added to cart successfully",
				true,
				updatedCart
		);

		return new ResponseEntity<>(res, HttpStatus.OK);
	}


}
