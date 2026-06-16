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
	private SmartCartConfigService smartCartConfigService;
	private com.proyecto.repositories.UserRepository userRepository;

	public CartServiceImplementation(
			CartRepository cartRepository,
			CartItemService cartItemService,
			ProductService productService,
			SmartCartConfigService smartCartConfigService,
			com.proyecto.repositories.UserRepository userRepository) {
		this.cartItemService=cartItemService;
		this.cartRepository=cartRepository;
		this.productService=productService;
		this.smartCartConfigService = smartCartConfigService;
		this.userRepository = userRepository;
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
		if (cart == null) {
			Usuario u = userRepository.findById(userId).orElse(null);
			if (u != null) {
				cart = createCart(u);
			}
		}
		Product product=productService.findProductById(req.getProductId());

		// --- VALIDACIÓN DE STOCK ---
		int stockDisponible = 0;
		boolean variantFound = false;

		if (product.getVariantes() != null && !product.getVariantes().isEmpty()) {
			for (com.proyecto.model.ProductVariant v : product.getVariantes()) {
				boolean matchesSize = v.getTalla() != null && v.getTalla().getValor().equalsIgnoreCase(req.getSize());
				boolean matchesColor = false;
				if (req.getColor() == null || req.getColor().trim().isEmpty() || req.getColor().equalsIgnoreCase("undefined")) {
					matchesColor = (v.getColor() == null);
				} else {
					matchesColor = (v.getColor() != null && req.getColor().equalsIgnoreCase(v.getColor().getNombre()));
				}
				if (matchesSize && matchesColor) {
					stockDisponible = v.getStock();
					variantFound = true;
					break;
				}
			}
		}

		if (!variantFound) {
			stockDisponible = 0;
		}

		if (stockDisponible < req.getQuantity()) {
			throw new CartItemException("Stock insuficiente para este producto en la variante seleccionada (Color: " 
				+ (req.getColor() != null ? req.getColor() : "N/A") + ", Talla: " + req.getSize() + "). Stock disponible: " + stockDisponible);
		}

		CartItem isPresent=cartItemService.isCartItemExist(cart, product, req.getSize(), req.getColor(), userId);

		if(isPresent==null) {
			CartItem cartItem=new CartItem();
			cartItem.setProduct(product);
			cartItem.setCart(cart);
			cartItem.setQuantity(req.getQuantity());
			cartItem.setUserId(userId);

			int price=req.getQuantity()*product.getDiscountedPrice();
			cartItem.setPrice(price);
			cartItem.setSize(req.getSize());
			cartItem.setColor(req.getColor());

			CartItem createdCartItem=cartItemService.createCartItem(cartItem);
			cart.getCartitems().add(createdCartItem);
		} else {
			// Si el artículo ya está presente, actualizamos su cantidad real en la base de datos
			isPresent.setQuantity(req.getQuantity());
			cartItemService.createCartItem(isPresent); // Esto recalculará price y discountedPrice y llamará a save
		}
		return "Item agregado al carrito";
	}

	@Override
	public String addCartItemUpsell(Long userId, AddItemRequest req) throws ProductException, CartItemException, com.proyecto.Exception.UserException {
		Cart cart = cartRepository.findByUserId(userId);
		if (cart == null) {
			Usuario u = userRepository.findById(userId).orElse(null);
			if (u != null) {
				cart = createCart(u);
			}
		}
		Product product = productService.findProductById(req.getProductId());

		// --- VALIDACIÓN DE STOCK ---
		int stockDisponible = 0;
		boolean variantFound = false;

		if (product.getVariantes() != null && !product.getVariantes().isEmpty()) {
			for (com.proyecto.model.ProductVariant v : product.getVariantes()) {
				boolean matchesSize = v.getTalla() != null && v.getTalla().getValor().equalsIgnoreCase(req.getSize());
				boolean matchesColor = false;
				if (req.getColor() == null || req.getColor().trim().isEmpty() || req.getColor().equalsIgnoreCase("undefined")) {
					matchesColor = (v.getColor() == null);
				} else {
					matchesColor = (v.getColor() != null && req.getColor().equalsIgnoreCase(v.getColor().getNombre()));
				}
				if (matchesSize && matchesColor) {
					stockDisponible = v.getStock();
					variantFound = true;
					break;
				}
			}
		}

		if (!variantFound) {
			stockDisponible = 0;
		}

		if (stockDisponible < req.getQuantity()) {
			throw new CartItemException("Stock insuficiente para este producto en la variante seleccionada (Color: " 
				+ (req.getColor() != null ? req.getColor() : "N/A") + ", Talla: " + req.getSize() + "). Stock disponible: " + stockDisponible);
		}

		CartItem isPresent = cartItemService.isCartItemExist(cart, product, req.getSize(), req.getColor(), userId);

		// Calculamos el precio con el descuento configurado
		com.proyecto.model.SmartCartConfig config = smartCartConfigService.getConfig();
		if (!config.isActive()) {
			throw new CartItemException("La configuración del carrito inteligente está desactivada.");
		}
		
		int basePrice = product.getDiscountedPrice();
		double discountMultiplier = (100.0 - config.getUpsellDiscountPercentage()) / 100.0;
		int upsellDiscountedPrice = (int) (basePrice * discountMultiplier);
		int price = req.getQuantity() * upsellDiscountedPrice;

		if (isPresent == null) {
			CartItem cartItem = new CartItem();
			cartItem.setProduct(product);
			cartItem.setCart(cart);
			cartItem.setQuantity(req.getQuantity());
			cartItem.setUserId(userId);

			cartItem.setPrice(price);
			cartItem.setDiscountedPrice(upsellDiscountedPrice); // Guardar el unitario con extra dinámico
			cartItem.setSize(req.getSize());
			cartItem.setColor(req.getColor());
			cartItem.setUpsell(true);

			CartItem createdCartItem = cartItemService.createCartItem(cartItem);
			
			// Sobrescribir precio después de crearlo si cartItemService lo recalcula
			createdCartItem.setPrice(price);
			createdCartItem.setDiscountedPrice(upsellDiscountedPrice);
			cartItemService.updateCartItem(userId, createdCartItem.getId(), createdCartItem); // Actualizar

			cart.getCartitems().add(createdCartItem);
		} else {
			// Si ya está, actualizamos cantidad y aplicamos el nuevo precio upsell
			isPresent.setQuantity(isPresent.getQuantity() + req.getQuantity());
			isPresent.setPrice(isPresent.getQuantity() * upsellDiscountedPrice);
			isPresent.setDiscountedPrice(upsellDiscountedPrice);
			cartItemService.updateCartItem(userId, isPresent.getId(), isPresent);
		}
		return "Item Upsell agregado al carrito con " + config.getUpsellDiscountPercentage() + "% extra";
	}

	@Override
	public Cart findUserCart(Long userdId) {

		Cart cart=cartRepository.findByUserId(userdId);
		if (cart == null) {
			Usuario u = userRepository.findById(userdId).orElse(null);
			if (u != null) {
				cart = createCart(u);
			}
		}

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

	@Override
	public void clearCart(Long userId) {
		Cart cart = cartRepository.findByUserId(userId);
		if (cart != null && cart.getCartitems() != null) {
			cart.getCartitems().clear();
			cart.setTotalPrice(0);
			cart.setTotalDiscountedPrice(0);
			cart.setTotalitem(0);
			cart.setDiscounte(0);
			cartRepository.save(cart);
		}
	}

}
