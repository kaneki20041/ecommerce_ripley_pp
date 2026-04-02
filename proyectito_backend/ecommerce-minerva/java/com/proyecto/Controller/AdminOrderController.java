package com.proyecto.Controller;

import java.util.List;

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
import com.proyecto.service.OrderService;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

	@Autowired
	private OrderService orderService;

	@GetMapping("/")
	public ResponseEntity<List<Order>>getAllOrdersHandler(){
		List<Order> orders=orderService.getAllOrders();
		return new ResponseEntity<>(orders,HttpStatus.ACCEPTED);
	}

	@PutMapping("/{orderId}/confirmed")
	public ResponseEntity<Order>ConfirmedOrderHandler(@PathVariable Long orderId,
			@RequestHeader("Authorization")String jwt) throws OrderException{
		Order order=orderService.confirmedOrder(orderId);

		return new ResponseEntity<>(order,HttpStatus.OK);
	}

	@PutMapping("/{orderId}/ship")
	public ResponseEntity<Order>ShippedOrderHandler(@PathVariable Long orderId,
			@RequestHeader("Authorization")String jwt) throws OrderException{
		Order order=orderService.shippedOrder(orderId);

		return new ResponseEntity<>(order,HttpStatus.OK);
	}

	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<Order>CancelOrderHandler(@PathVariable Long orderId,
			@RequestHeader("Authorization")String jwt) throws OrderException{
		Order order=orderService.cancledOrder(orderId);

		return new ResponseEntity<>(order,HttpStatus.OK);
	}

	@PutMapping("/{orderId}/delete")
	public ResponseEntity<ApiResponse>DeleteOrderHandler(@PathVariable Long orderId,
			@RequestHeader("Authorization")String jwt) throws OrderException{

		orderService.deleteOrder(orderId);

		ApiResponse res=new ApiResponse();
		res.setMessage("order eliminado correctamente");
		res.setResult(true);

		return new ResponseEntity<>(res,HttpStatus.OK);
	}


}
