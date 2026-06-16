package com.proyecto.service;

import com.proyecto.Exception.CartItemException;
import com.proyecto.Exception.ProductException;
import com.proyecto.model.Cart;
import com.proyecto.model.Usuario;
import com.proyecto.request.AddItemRequest;

public interface CartService {

	public Cart createCart(Usuario user);

	public String addCartItem(Long userId,AddItemRequest req) throws ProductException,CartItemException;
	
	public String addCartItemUpsell(Long userId, AddItemRequest req) throws ProductException, CartItemException, com.proyecto.Exception.UserException;

	public Cart findUserCart(Long userId);
	public void clearCart(Long userId);
}
