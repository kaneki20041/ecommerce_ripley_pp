package com.proyecto.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.OrderException;
import com.proyecto.Exception.UserException;
import com.proyecto.model.Address;
import com.proyecto.model.Order;
import com.proyecto.model.Usuario;
import com.proyecto.response.ApiResponse;
import com.proyecto.service.OrderService;
import com.proyecto.service.UserService;

import java.util.stream.Collectors;
import com.proyecto.request.OrderRequest;
import com.proyecto.response.OrderResponse;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;

	@Autowired
	private com.proyecto.service.ShippoService shippoService;

	@PostMapping("/")
	public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@RequestBody OrderRequest req, @RequestHeader("Authorization") String jwt) throws UserException {
		Usuario user = userService.findUserProfileByJwt(jwt);
		Order order = orderService.createOrderWithCoupon(user, req.getShippingAddress(), req.getShippingMethod(), req.getStoreName(), req.getCouponCode());
		OrderResponse orderRes = new OrderResponse(order);
		ApiResponse<OrderResponse> response = new ApiResponse<>("Pedido creado con éxito", true, orderRes);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping("/user")
	public ResponseEntity<ApiResponse<List<OrderResponse>>> usersOrderHistory(@RequestHeader("Authorization") String jwt) throws UserException {
		Usuario user = userService.findUserProfileByJwt(jwt);
		List<Order> orders = orderService.usersOrderHistory(user.getId());
		List<OrderResponse> ordersRes = orders.stream()
				.map(OrderResponse::new)
				.collect(Collectors.toList());
		ApiResponse<List<OrderResponse>> response = new ApiResponse<>("Historial de pedidos recuperado con éxito", true, ordersRes);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/{Id}")
	public ResponseEntity<ApiResponse<OrderResponse>> findOrderById(@PathVariable("Id") Long orderId, @RequestHeader("Authorization") String jwt) throws UserException, OrderException {
		Usuario user = userService.findUserProfileByJwt(jwt);
		Order order = orderService.findOrderByld(orderId);
		OrderResponse orderRes = new OrderResponse(order);
		ApiResponse<OrderResponse> response = new ApiResponse<>("Pedido encontrado", true, orderRes);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/{Id}/tracking")
	public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getOrderTracking(
			@PathVariable("Id") Long orderId,
			@RequestHeader("Authorization") String jwt) throws UserException, OrderException {
		userService.findUserProfileByJwt(jwt); // Validar sesión
		Order order = orderService.findOrderByld(orderId);
		
		String trackingNum = order.getTrackingNumber();
		if (trackingNum == null || trackingNum.isEmpty()) {
			ApiResponse<java.util.Map<String, Object>> errorRes = new ApiResponse<>("El pedido aún no tiene información de seguimiento.", false);
			return new ResponseEntity<>(errorRes, HttpStatus.OK);
		}
		
		java.util.Map<String, Object> trackingData = shippoService.getTrackingStatus(trackingNum, order.getOrderStatus());
		ApiResponse<java.util.Map<String, Object>> response = new ApiResponse<>("Seguimiento del pedido recuperado con éxito", true, trackingData);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}




