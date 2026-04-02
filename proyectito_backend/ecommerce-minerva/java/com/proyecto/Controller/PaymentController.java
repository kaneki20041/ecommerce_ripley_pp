package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.repositories.OrderRepository;
import com.proyecto.service.OrderService;
import com.proyecto.service.UserService;

@RestController
@RequestMapping("/api")
public class PaymentController {

	@Autowired
	private OrderService orderService;
	@Autowired
	private UserService userService;
	@Autowired
	private OrderRepository orderRepository;
	//
	//@PostMapping("/payments/{orderId}")
	//public ResponseEntity<PaymentLinkResponse>createPaymentLink(
	//		@PathVariable Long orderId,
	//		@RequestHeader("Authorization")String jwt)throws MercadopagoException,UserException,OrderException{
	//
	//	Order order=orderService.findOrderByld(orderId);

	//	try {

	//		MercadopagoClient mercadopago=new MercadopagoClient("APP_USR-adacba2f-5ffe-4ce7-b66c-8418ecfa0d1d","APP_USR-8251566499520093-111800-b6c5b49e85eb815987b344ebbe97bc15-266466636");
	//	}catch(Exception e) {

	//	}

	//}
}
