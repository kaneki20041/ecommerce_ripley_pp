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

@Service
public class OrderServiceImplementation implements OrderService{

	private OrderRepository orderRepository;
	private CartService cartService;
	private AddressRepository addressRepository;
	private UserRepository userRepository;
	private OrderItemService orderItemService;
	private OrderItemRepository orderItemRepository;

	public OrderServiceImplementation(OrderRepository orderRepository, CartService cartService, AddressRepository addressRepository, UserRepository userRepository, OrderItemService orderItemService,OrderItemRepository orderItemRepository) {
	this.orderRepository=orderRepository;
	this.cartService=cartService;
	this.addressRepository=addressRepository;
	this.userRepository=userRepository;
	this.orderItemService=orderItemService;
	this.orderItemRepository=orderItemRepository;

	}

	@Transactional
	@Override
	public Order createOrder(Usuario user, Address shippAddress) {

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
	        orderItem.setId(item.getUserId());
	        orderItem.setDiscountedPrice(item.getDiscountedPrice());

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

	    Order savedOrder = orderRepository.save(createdOrder);

	    for (OrderItem item : orderItems) {
	        item.setOrder(savedOrder);
	        orderItemRepository.save(item);
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
		Order order=findOrderByld (orderld);
		order.setOrderStatus("SHIPPED");
		return orderRepository.save(order);
	}

	@Override
	public Order deliveredOrder(Long orderld) throws OrderException {
		Order order=findOrderByld(orderld);
		order.setOrderStatus("DELIVERED");
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
