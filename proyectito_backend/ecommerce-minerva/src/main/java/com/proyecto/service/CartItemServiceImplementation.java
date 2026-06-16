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

import com.proyecto.model.ProductVariant;

@Service
public class CartItemServiceImplementation implements CartItemService{

	private CartItemRepository cartItemRepository;
	private UserService userService;
	private CartRepository cartRepository;
	private SmartCartConfigService smartCartConfigService;

	public CartItemServiceImplementation(
			CartItemRepository cartItemRepository,
			UserService userService,CartRepository cartRepository,
			SmartCartConfigService smartCartConfigService) {
		this.cartItemRepository=cartItemRepository;
		this.userService=userService;
		this.setCartRepository(cartRepository);
		this.smartCartConfigService = smartCartConfigService;
	}

	private ProductVariant findMatchingVariant(Product product, String size, String color) {
		if (product == null || product.getVariantes() == null || size == null) {
			return null;
		}
		for (ProductVariant v : product.getVariantes()) {
			boolean matchesSize = v.getTalla() != null && v.getTalla().getValor().equalsIgnoreCase(size);
			boolean matchesColor = false;
			if (color == null || color.trim().isEmpty() || color.equalsIgnoreCase("undefined")) {
				matchesColor = (v.getColor() == null);
			} else {
				matchesColor = (v.getColor() != null && color.equalsIgnoreCase(v.getColor().getNombre()));
			}
			if (matchesSize && matchesColor) {
				return v;
			}
		}
		return null;
	}

	@Override
	public CartItem createCartItem(CartItem cartItem) throws CartItemException {

		if (cartItem.getUserId() == null) {
			throw new CartItemException("No se puede crear un CartItem sin un userId asignado.");
		}

		if (cartItem.getQuantity() <= 0) {
			cartItem.setQuantity(1);
		}

		ProductVariant variant = findMatchingVariant(cartItem.getProduct(), cartItem.getSize(), cartItem.getColor());
		int unitPrice = (variant != null) ? variant.getPrice() : cartItem.getProduct().getPrice();
		int unitDiscounted = (variant != null) ? variant.getDiscountedPrice() : cartItem.getProduct().getDiscountedPrice();

		if (cartItem.isUpsell()) {
			com.proyecto.model.SmartCartConfig config = smartCartConfigService.getConfig();
			if (config.isActive()) {
				double multiplier = (100.0 - config.getUpsellDiscountPercentage()) / 100.0;
				unitDiscounted = (int) (unitDiscounted * multiplier);
			}
		}

		cartItem.setPrice(unitPrice * cartItem.getQuantity());
		cartItem.setDiscountedPrice(unitDiscounted * cartItem.getQuantity());

		return cartItemRepository.save(cartItem);
	}

	@Override
	public CartItem updateCartItem(Long userId, Long id, CartItem cartItem) throws CartItemException, UserException {

		CartItem item = findCartItemById(id);

		if(item.getUserId().equals(userId)) {
			item.setQuantity(cartItem.getQuantity());
			ProductVariant variant = findMatchingVariant(item.getProduct(), item.getSize(), item.getColor());
			int unitPrice = (variant != null) ? variant.getPrice() : item.getProduct().getPrice();
			int unitDiscounted = (variant != null) ? variant.getDiscountedPrice() : item.getProduct().getDiscountedPrice();

			if (item.isUpsell()) {
				com.proyecto.model.SmartCartConfig config = smartCartConfigService.getConfig();
				if (config.isActive()) {
					double multiplier = (100.0 - config.getUpsellDiscountPercentage()) / 100.0;
					unitDiscounted = (int) (unitDiscounted * multiplier);
				}
			}

			item.setPrice(unitPrice * item.getQuantity());
			item.setDiscountedPrice(unitDiscounted * item.getQuantity());

			return cartItemRepository.save(item);
		} else {
			throw new UserException("No puedes actualizar los items del carrito de otro usuario");
		}
	}

	@Override
	public CartItem isCartItemExist(Cart cart, Product product, String size, String color, Long userId) {

		CartItem cartItem=cartItemRepository.isCartItemExist(cart, product, size, color, userId);

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
