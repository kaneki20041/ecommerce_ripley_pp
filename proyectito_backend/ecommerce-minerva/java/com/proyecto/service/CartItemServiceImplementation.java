package com.proyecto.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.Exception.CartItemException;
import com.proyecto.Exception.UserException;
import com.proyecto.model.Cart;
import com.proyecto.model.CartItem;
import com.proyecto.model.Product;
import com.proyecto.repositories.CartItemRepository;
import com.proyecto.repositories.CartRepository;

@Service
public class CartItemServiceImplementation implements CartItemService{

	private CartItemRepository cartItemRepository;
	private UserService userService;
	private CartRepository cartRepository;

	public CartItemServiceImplementation(
			CartItemRepository cartItemRepository,
			UserService userService,CartRepository cartRepository) {
		this.cartItemRepository=cartItemRepository;
		this.userService=userService;
		this.setCartRepository(cartRepository);
	}

	@Override
	public CartItem createCartItem(CartItem cartItem) throws CartItemException {

		if (cartItem.getUserId() == null) {
			throw new CartItemException("No se puede crear un CartItem sin un userId asignado.");
		}


		cartItem.setQuantity(1);
		cartItem.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity());
		cartItem.setDiscountedPrice(cartItem.getProduct().getDescuentoprice() * cartItem.getQuantity());

		return cartItemRepository.save(cartItem);
	}

	@Override
	public CartItem updateCartItem(Long userId, Long id, CartItem cartItem) throws CartItemException, UserException {

		CartItem item = findCartItemById(id);

		if(item.getUserId().equals(userId)) {
			item.setQuantity(cartItem.getQuantity());
			item.setPrice(item.getQuantity() * item.getProduct().getPrice());
			item.setDiscountedPrice(item.getProduct().getDescuentoprice() * item.getQuantity());

			return cartItemRepository.save(item);
		} else {
			throw new UserException("No puedes actualizar los items del carrito de otro usuario");
		}
	}

	@Override
	public CartItem isCartItemExist(Cart cart, Product product, String size, Long userId) {

		CartItem cartItem=cartItemRepository.isCartItemExist(cart, product, size, userId);

		return cartItem;
	}

	@Override
    public void removerCartItem(Long userId, Long cartItemId) throws CartItemException, UserException {

        CartItem cartItem = findCartItemById(cartItemId);

        if (cartItem.getUserId().equals(userId)) {
            cartItemRepository.deleteById(cartItemId);
        } else {
            throw new UserException("No puedes remover los items del carrito de otros usuarios");
        }
    }
	@Override
	public CartItem findCartItemById(Long cartItemId) throws CartItemException {

		Optional<CartItem>opt=cartItemRepository.findById(cartItemId);

		if(opt.isPresent()) {
			return opt.get();
		}
		throw new CartItemException("El item del carrito no fue encontrado con el id:"+cartItemId);
	}

	public CartRepository getCartRepository() {
		return cartRepository;
	}

	public void setCartRepository(CartRepository cartRepository) {
		this.cartRepository = cartRepository;
	}


}
