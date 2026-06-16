package com.proyecto.service;

import java.util.List;

import com.proyecto.Exception.OrderException;
import com.proyecto.model.Address;
import com.proyecto.model.Order;
import com.proyecto.model.Usuario;

public interface OrderService {

	public Order createOrder(Usuario user, Address shippingAdress, String shippingMethod, String storeName);
	public Order createOrderWithCoupon(Usuario user, Address shippingAdress, String shippingMethod, String storeName, String couponCode);
	public Order findOrderByld(Long orderld) throws OrderException;
	public List<Order> usersOrderHistory(Long userld);
	public Order placedOrder(Long orderld) throws OrderException;
	public Order confirmedOrder(Long orderld)throws OrderException;
	public Order shippedOrder(Long orderld) throws OrderException;
	public Order deliveredOrder(Long orderld) throws OrderException;
	public Order cancledOrder(Long orderld) throws OrderException;
	public List<Order> getAllOrders();
	public void deleteOrder(Long orderld) throws OrderException;
}
