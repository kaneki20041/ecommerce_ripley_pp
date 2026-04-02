package com.proyecto.service;

import com.proyecto.Exception.CartItemException;
import com.proyecto.Exception.UserException;
import com.proyecto.model.Cart;
import com.proyecto.model.CartItem;
import com.proyecto.model.Product;

public interface CartItemService {

	public CartItem createCartItem(CartItem cartItem) throws CartItemException;

	public CartItem updateCartItem(Long userId, Long id, CartItem cartItem) throws CartItemException, UserException;

	public CartItem isCartItemExist(Cart cart,Product product,String size,Long userId);

	public void removerCartItem(Long userId, Long cartItemId) throws CartItemException, UserException;
	public CartItem findCartItemById(Long cartItemId)throws CartItemException;
}
