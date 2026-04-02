package com.proyecto.service;

import org.springframework.stereotype.Service;

import com.proyecto.Exception.CartItemException;
import com.proyecto.Exception.ProductException;
import com.proyecto.model.Cart;
import com.proyecto.model.CartItem;
import com.proyecto.model.Product;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.CartRepository;
import com.proyecto.request.AddItemRequest;

@Service
public class CartServiceImplementation implements CartService{

	private CartRepository cartRepository;
	private CartItemService cartItemService;
	private ProductService productService;

	public CartServiceImplementation(
			CartRepository cartRepository,
			CartItemService cartItemService,
			ProductService productService) {
		this.cartItemService=cartItemService;
		this.cartRepository=cartRepository;
		this.productService=productService;
	}

	@Override
	public Cart createCart(Usuario user) {

		Cart cart=new Cart();
		cart.setUser(user);
		return cartRepository.save(cart);
	}

	@Override
	public String addCartItem(Long userId, AddItemRequest req) throws ProductException,CartItemException {
		Cart cart=cartRepository.findByUserId(userId);
		Product product=productService.findProductById(req.getProductId());

		CartItem isPresent=cartItemService.isCartItemExist(cart, product, req.getSize(), userId);

		if(isPresent==null) {
			CartItem cartItem=new CartItem();
			cartItem.setProduct(product);
			cartItem.setCart(cart);
			cartItem.setQuantity(req.getQuantity());
			cartItem.setUserId(userId);

			int price=req.getQuantity()*product.getDescuentoprice();
			cartItem.setPrice(price);
			cartItem.setSize(req.getSize());

			CartItem createdCartItem=cartItemService.createCartItem(cartItem);
			cart.getCartitems().add(createdCartItem);
		}
		return "Item agregado al carrito";
	}

	@Override
	public Cart findUserCart(Long userdId) {

		Cart cart=cartRepository.findByUserId(userdId);

		int totalPrice=0;
		int totalDiscountedPrice=0;
		int totalItem=0;


		for(CartItem cartItem :cart.getCartitems()) {
			totalPrice=totalPrice+cartItem.getPrice();
			totalDiscountedPrice=totalDiscountedPrice+cartItem.getDiscountedPrice();
			totalItem=totalItem+cartItem.getQuantity();
		}

		cart.setTotalDiscountedPrice(totalDiscountedPrice);
		cart.setTotalitem(totalItem);
		cart.setTotalPrice(totalPrice);
		cart.setDiscounte(totalDiscountedPrice);

		return cartRepository.save(cart);
	}

}
