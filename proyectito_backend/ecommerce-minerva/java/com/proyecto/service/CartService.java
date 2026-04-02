package com.proyecto.service;

import com.proyecto.Exception.CartItemException;
import com.proyecto.Exception.ProductException;
import com.proyecto.model.Cart;
import com.proyecto.model.Usuario;
import com.proyecto.request.AddItemRequest;

public interface CartService {

	public Cart createCart(Usuario user);

	public String addCartItem(Long userId, AddItemRequest req) throws ProductException, CartItemException;
	public Cart findUserCart(Long userdId);
}
