package com.proyecto.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.OrderException;
import com.proyecto.model.Order;
import com.proyecto.response.ApiResponse;
import com.proyecto.response.AdminOrderListResponse;
import com.proyecto.response.AdminOrderDetailResponse;
import com.proyecto.service.OrderService;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

	@Autowired
	private OrderService orderService;

	@GetMapping("/")
	public ResponseEntity<ApiResponse<List<AdminOrderListResponse>>> getAllOrdersHandler() {
		List<Order> orders = orderService.getAllOrders();
		
		List<AdminOrderListResponse> dtoOrders = orders.stream().map(o -> {
			AdminOrderListResponse dto = new AdminOrderListResponse();
			dto.setId(o.getId());
			dto.setOrderId(o.getOrderId());
			dto.setOrderStatus(o.getOrderStatus());
			dto.setTotalPrice(o.getTotalPrice());
			dto.setTotalDiscountedPrice(o.getTotalDiscountedPrice());
			dto.setShippingMethod(o.getShippingMethod());
			dto.setStoreName(o.getStoreName());
			
			if (o.getShippingAddress() != null) {
				var addr = o.getShippingAddress();
				dto.setCustomerName((addr.getFirstName() + " " + addr.getLastName()).trim());
			} else {
				dto.setCustomerName("Usuario Minerva");
			}

			dto.setOrderDate(o.getCreateAt() != null ? o.getCreateAt().toString().split("T")[0] 
				: (o.getOrderDate() != null ? o.getOrderDate().toString().split("T")[0] : "Hoy"));
			
			return dto;
		}).collect(Collectors.toList());

		ApiResponse<List<AdminOrderListResponse>> res = new ApiResponse<>("Pedidos recuperados con éxito", true, dtoOrders);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<ApiResponse<AdminOrderDetailResponse>> getOrderByIdHandler(@PathVariable Long orderId,
			@RequestHeader("Authorization") String jwt) throws OrderException {
		Order o = orderService.findOrderByld(orderId);
		
		AdminOrderDetailResponse dto = new AdminOrderDetailResponse();
		dto.setId(o.getId());
		dto.setOrderId(o.getOrderId());
		dto.setOrderStatus(o.getOrderStatus());
		dto.setTotalPrice(o.getTotalPrice());
		dto.setTotalDiscountedPrice(o.getTotalDiscountedPrice());
		dto.setEmail(o.getUser() != null ? o.getUser().getEmail() : "No especificado");
		dto.setShippingMethod(o.getShippingMethod());
		dto.setStoreName(o.getStoreName());
		
		if (o.getShippingAddress() != null) {
			var addr = o.getShippingAddress();
			dto.setCustomerName((addr.getFirstName() + " " + addr.getLastName()).trim());
			dto.setPhone(addr.getCelular());
			
			String street = addr.getStreetAddress() != null ? addr.getStreetAddress() : "";
			String city = addr.getCity() != null ? ", " + addr.getCity() : "";
			String state = addr.getState() != null ? ", " + addr.getState() : "";
			String zip = addr.getZipCode() != null ? " (" + addr.getZipCode() + ")" : "";
			dto.setShippingAddress(street + city + state + zip);
		} else {
			dto.setCustomerName("Usuario Minerva");
			dto.setPhone("No especificado");
			dto.setShippingAddress("No especificada");
		}

		dto.setPaymentMethod(o.getPaymentDetails() != null && o.getPaymentDetails().getPaymentMethod() != null 
			? o.getPaymentDetails().getPaymentMethod() : "Mercado Pago (Simulado)");
			
		dto.setOrderDate(o.getCreateAt() != null ? o.getCreateAt().toString().split("T")[0] 
			: (o.getOrderDate() != null ? o.getOrderDate().toString().split("T")[0] : "Hoy"));
		
		List<AdminOrderDetailResponse.AdminOrderItemDetailResponse> items = o.getOrderItems().stream().map(item -> {
			String prodTitle = item.getProduct() != null ? item.getProduct().getTitle() : "Producto";
			String size = item.getSize() != null ? " / Talla: " + item.getSize() : "";
			String color = item.getColor() != null ? " / Color: " + item.getColor() : "";
			double price = item.getDiscountedPrice() != 0 ? item.getDiscountedPrice() : item.getPrice();
			return new AdminOrderDetailResponse.AdminOrderItemDetailResponse(
				prodTitle + color + size,
				item.getQuantity(),
				price
			);
		}).collect(Collectors.toList());
		
		dto.setTrackingNumber(o.getTrackingNumber());
		dto.setShippingLabelUrl(o.getShippingLabelUrl());
		dto.setProducts(items);

		ApiResponse<AdminOrderDetailResponse> res = new ApiResponse<>("Detalle del pedido recuperado con éxito", true, dto);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@PutMapping("/{orderId}/confirmed")
	public ResponseEntity<ApiResponse<com.proyecto.response.OrderResponse>> ConfirmedOrderHandler(@PathVariable Long orderId,
			@RequestHeader("Authorization") String jwt) throws OrderException {
		Order order = orderService.confirmedOrder(orderId);
		com.proyecto.response.OrderResponse orderRes = new com.proyecto.response.OrderResponse(order);
		ApiResponse<com.proyecto.response.OrderResponse> res = new ApiResponse<>("Pedido confirmado con éxito", true, orderRes);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@PutMapping("/{orderId}/ship")
	public ResponseEntity<ApiResponse<com.proyecto.response.OrderResponse>> ShippedOrderHandler(@PathVariable Long orderId,
			@RequestHeader("Authorization") String jwt) throws OrderException {
		Order order = orderService.shippedOrder(orderId);
		com.proyecto.response.OrderResponse orderRes = new com.proyecto.response.OrderResponse(order);
		ApiResponse<com.proyecto.response.OrderResponse> res = new ApiResponse<>("Pedido enviado con éxito", true, orderRes);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@PutMapping("/{orderId}/deliver")
	public ResponseEntity<ApiResponse<com.proyecto.response.OrderResponse>> DeliveredOrderHandler(@PathVariable Long orderId,
			@RequestHeader("Authorization") String jwt) throws OrderException {
		Order order = orderService.deliveredOrder(orderId);
		com.proyecto.response.OrderResponse orderRes = new com.proyecto.response.OrderResponse(order);
		ApiResponse<com.proyecto.response.OrderResponse> res = new ApiResponse<>("Pedido entregado con éxito", true, orderRes);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<ApiResponse<com.proyecto.response.OrderResponse>> CancelOrderHandler(@PathVariable Long orderId,
			@RequestHeader("Authorization") String jwt) throws OrderException {
		Order order = orderService.cancledOrder(orderId);
		com.proyecto.response.OrderResponse orderRes = new com.proyecto.response.OrderResponse(order);
		ApiResponse<com.proyecto.response.OrderResponse> res = new ApiResponse<>("Pedido cancelado con éxito", true, orderRes);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

	@PutMapping("/{orderId}/delete")
	public ResponseEntity<ApiResponse<Long>> DeleteOrderHandler(@PathVariable Long orderId,
			@RequestHeader("Authorization") String jwt) throws OrderException {
		orderService.deleteOrder(orderId);
		ApiResponse<Long> res = new ApiResponse<>("Pedido eliminado correctamente", true, orderId);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}

}

