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
import com.proyecto.service.OrderService;
import com.proyecto.service.UserService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;

	@PostMapping("/")
	public ResponseEntity<Order> createOrder(@RequestBody Address shippingAddress,@RequestHeader("Authorization")String jwt) throws UserException{

		Usuario user=userService.findUserProfileByJwt(jwt);
		Order order=orderService.createOrder(user, shippingAddress);
		System.out.println("order"+ order);
		return new ResponseEntity<>(order,HttpStatus.CREATED);
	}

	@GetMapping("/user")
	public ResponseEntity<List<Order>>usersOrderHistory(@RequestHeader("Authorization")String jwt)throws UserException{

		Usuario user=userService.findUserProfileByJwt(jwt);

		List<Order> orders=orderService.usersOrderHistory(user.getId());

		return new ResponseEntity<>(orders,HttpStatus.CREATED);
	}

	@GetMapping("/{Id}")
	public ResponseEntity<Order>findOrderById(@PathVariable("Id")Long orderId,@RequestHeader("Authorization")String jwt)throws UserException,OrderException{

		Usuario user=userService.findUserProfileByJwt(jwt);

		Order order=orderService.findOrderByld(orderId);

		return new ResponseEntity<>(order,HttpStatus.CREATED);
	}


}




