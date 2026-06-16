package com.proyecto.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.Exception.OrderException;
import com.proyecto.model.Address;
import com.proyecto.model.Cart;
import com.proyecto.model.CartItem;
import com.proyecto.model.Order;
import com.proyecto.model.OrderItem;
import com.proyecto.model.PaymentDetails;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.AddressRepository;
import com.proyecto.repositories.OrderItemRepository;
import com.proyecto.repositories.OrderRepository;
import com.proyecto.repositories.UserRepository;
import com.proyecto.repositories.ProductVariantRepository;

@Service
public class OrderServiceImplementation implements OrderService{

	private OrderRepository orderRepository;
	private CartService cartService;
	private AddressRepository addressRepository;
	private UserRepository userRepository;
	private OrderItemService orderItemService;
	private OrderItemRepository orderItemRepository;
	private com.proyecto.repositories.CartRepository cartRepository;
	private com.proyecto.repositories.CartItemRepository cartItemRepository;
	private ProductVariantRepository productVariantRepository;
	private CouponService couponService;

	@org.springframework.beans.factory.annotation.Autowired
	private ShippoService shippoService;

	public OrderServiceImplementation(
			OrderRepository orderRepository,
			CartService cartService,
			AddressRepository addressRepository,
			UserRepository userRepository,
			OrderItemService orderItemService,
			OrderItemRepository orderItemRepository,
			com.proyecto.repositories.CartRepository cartRepository,
			com.proyecto.repositories.CartItemRepository cartItemRepository,
			ProductVariantRepository productVariantRepository,
			CouponService couponService) {
		this.orderRepository=orderRepository;
		this.cartService=cartService;
		this.addressRepository=addressRepository;
		this.userRepository=userRepository;
		this.orderItemService=orderItemService;
		this.orderItemRepository=orderItemRepository;
		this.cartRepository=cartRepository;
		this.cartItemRepository=cartItemRepository;
		this.productVariantRepository=productVariantRepository;
		this.couponService=couponService;
	}

	@Transactional
	@Override
	public Order createOrder(Usuario user, Address shippAddress, String shippingMethod, String storeName) {
		// Este método está aquí para no romper la interfaz antigua si se usa en otros lados,
		// pero idealmente deberíamos usar el nuevo método que acepta couponCode.
		return createOrderWithCoupon(user, shippAddress, shippingMethod, storeName, null);
	}

	@Transactional
	public Order createOrderWithCoupon(Usuario user, Address shippAddress, String shippingMethod, String storeName, String couponCode) {

	    shippAddress.setUsuario(user);
	    Address address = addressRepository.save(shippAddress);
	    user.getAddress().add(address);
	    userRepository.save(user);

	    Cart cart = cartService.findUserCart(user.getId());
	    List<OrderItem> orderItems = new ArrayList<>();

	    for (CartItem item : cart.getCartitems()) {
	        OrderItem orderItem = new OrderItem();

	        orderItem.setPrice(item.getPrice());
	        orderItem.setProduct(item.getProduct());
	        orderItem.setQuantity(item.getQuantity());
	        orderItem.setSize(item.getSize());
	        orderItem.setColor(item.getColor());
	        orderItem.setDiscountedPrice(item.getDiscountedPrice());

	        // --- DESCUENTO DE STOCK EN BASE DE DATOS ---
	        com.proyecto.model.Product product = item.getProduct();
	        com.proyecto.model.ProductVariant matchedVariant = null;
	        if (product.getVariantes() != null) {
	            for (com.proyecto.model.ProductVariant v : product.getVariantes()) {
	                boolean matchesSize = v.getTalla() != null && v.getTalla().getValor().equalsIgnoreCase(item.getSize());
	                boolean matchesColor = false;
	                if (item.getColor() == null || item.getColor().trim().isEmpty() || item.getColor().equalsIgnoreCase("undefined")) {
	                    matchesColor = (v.getColor() == null);
	                } else {
	                    matchesColor = (v.getColor() != null && item.getColor().equalsIgnoreCase(v.getColor().getNombre()));
	                }
	                if (matchesSize && matchesColor) {
	                    matchedVariant = v;
	                    break;
	                }
	            }
	        }

	        if (matchedVariant != null) {
	            int currentStock = matchedVariant.getStock();
	            if (currentStock < item.getQuantity()) {
	                throw new RuntimeException("Stock insuficiente para el producto: " + product.getTitle() + 
	                    " (Color: " + (item.getColor() != null ? item.getColor() : "N/A") + 
	                    ", Talla: " + item.getSize() + "). Stock disponible: " + currentStock);
	            }
	            matchedVariant.setStock(currentStock - item.getQuantity());
	            productVariantRepository.save(matchedVariant);
	        }

	        OrderItem createdOrderItem = orderItemRepository.save(orderItem);
	        orderItems.add(createdOrderItem);
	    }

	    Order createdOrder = new Order();
	    createdOrder.setUser(user);
	    createdOrder.setOrderItems(orderItems);
	    createdOrder.setTotalPrice(cart.getTotalPrice());
	    createdOrder.setTotalDiscountedPrice(cart.getTotalDiscountedPrice());
	    createdOrder.setDiscounte(cart.getDiscounte());
	    createdOrder.setTotalItem(cart.getTotalitem());
	    createdOrder.setShippingAddress(address);
	    createdOrder.setOrderDate(LocalDateTime.now());
	    createdOrder.setOrderStatus("PENDING");
	    createdOrder.setPaymentDetails(new PaymentDetails()); // Asegúrate de inicializar PaymentDetails si es null
	    createdOrder.getPaymentDetails().setStatus("PENDING");
	    createdOrder.setCreateAt(LocalDateTime.now());
	    createdOrder.setShippingMethod(shippingMethod);
	    createdOrder.setStoreName(storeName);

	    if (couponCode != null && !couponCode.isEmpty()) {
	        try {
	            couponService.validateCoupon(couponCode, cart.getTotalDiscountedPrice());
	            com.proyecto.model.Coupon coupon = couponService.getCouponByCode(couponCode);
	            createdOrder.setCouponCode(coupon.getCode());
	            
	            // Calcular el descuento extra (excluyendo items de upsell)
	            int eligibleDiscountedPrice = 0;
	            for (CartItem item : cart.getCartitems()) {
	                if (!item.isUpsell()) {
	                    eligibleDiscountedPrice += item.getDiscountedPrice();
	                }
	            }
	            
	            double extraDiscount = 0;
	            if ("percentage".equalsIgnoreCase(coupon.getType())) {
	                extraDiscount = eligibleDiscountedPrice * (coupon.getDiscount() / 100.0);
	            } else {
	                extraDiscount = coupon.getDiscount();
	                if (extraDiscount > eligibleDiscountedPrice) {
	                    extraDiscount = eligibleDiscountedPrice; // Evitar descuento mayor al subtotal elegible
	                }
	            }
	            
	            int finalDiscountedPrice = cart.getTotalDiscountedPrice() - (int)Math.round(extraDiscount);
	            if (finalDiscountedPrice < 0) finalDiscountedPrice = 0;
	            
	            createdOrder.setTotalDiscountedPrice(finalDiscountedPrice);
	            createdOrder.setDiscounte(cart.getDiscounte() + (int)Math.round(extraDiscount));
	            
	            couponService.incrementUsage(couponCode);
	        } catch (Exception e) {
	            System.out.println("No se pudo aplicar el cupón en la orden: " + e.getMessage());
	        }
	    }

	    Order savedOrder = orderRepository.save(createdOrder);

	    for (OrderItem item : orderItems) {
	        item.setOrder(savedOrder);
	        orderItemRepository.save(item);
	    }

	    // Vaciar el carrito en el backend después de crear la orden
	    if (cart != null && cart.getCartitems() != null) {
	        cartItemRepository.deleteAll(cart.getCartitems());
	        cart.getCartitems().clear();
	        cart.setTotalPrice(0);
	        cart.setTotalDiscountedPrice(0);
	        cart.setTotalitem(0);
	        cart.setDiscounte(0);
	        cartRepository.save(cart);
	    }

	    return savedOrder;
	}


	@Override
	public Order findOrderByld(Long orderld) throws OrderException {
		Optional<Order> opt=orderRepository.findById(orderld);
		if(opt.isPresent()) {
			return opt.get();
		}
		throw new OrderException("order not exist with id "+orderld);
	}

	@Override
	public List<Order> usersOrderHistory(Long userld) {
		List<Order> orders=orderRepository.getUsersOrders(userld);
		return orders;
	}

	@Override
	public Order placedOrder(Long orderld) throws OrderException {
		Order order=findOrderByld(orderld);
		order.setOrderStatus("PLACED");
		order.getPaymentDetails().setStatus("COMPLETED");
		return order;
	}

	@Override
	public Order confirmedOrder(Long orderld) throws OrderException {
		Order order=findOrderByld(orderld);
		order.setOrderStatus("CONFIRMED");
		return orderRepository.save(order);
	}

	@Override
	public Order shippedOrder(Long orderld) throws OrderException {
		Order order = findOrderByld(orderld);
		if ("TIENDA".equalsIgnoreCase(order.getShippingMethod())) {
			order.setOrderStatus("READY_FOR_PICKUP");
		} else {
			order.setOrderStatus("SHIPPED");
			// Generar envío en Shippo y guardar tracking + etiqueta
			ShippoService.ShippoResponse shippoRes = shippoService.createShipment(order);
			order.setTrackingNumber(shippoRes.getTrackingNumber());
			order.setShippingLabelUrl(shippoRes.getShippingLabelUrl());
		}
		return orderRepository.save(order);
	}

	@Override
	public Order deliveredOrder(Long orderld) throws OrderException {
		Order order = findOrderByld(orderld);
		if ("TIENDA".equalsIgnoreCase(order.getShippingMethod())) {
			order.setOrderStatus("PICKED_UP");
		} else {
			order.setOrderStatus("DELIVERED");
		}
		return orderRepository.save(order);
	}

	@Override
	public Order cancledOrder(Long orderld) throws OrderException {

		Order order=findOrderByld(orderld);
		order.setOrderStatus("CANCELLED");
		return orderRepository.save(order);
	}

	@Override
	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	public void deleteOrder(Long orderld) throws OrderException {
		Order order =findOrderByld(orderld);
		orderRepository.deleteById(orderld);

	}


}
