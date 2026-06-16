package com.proyecto.Controller;

import com.proyecto.Exception.OrderException;
import com.proyecto.Exception.UserException;
import com.proyecto.model.OrderRequest;
import com.proyecto.model.Usuario;
import com.proyecto.request.CreateOrderRequestDto;
import com.proyecto.service.OrderRequestService;
import com.proyecto.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class UserOrderRequestController {

    @Autowired
    private OrderRequestService orderRequestService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<OrderRequest> createRequest(
            @RequestBody CreateOrderRequestDto req,
            @RequestHeader("Authorization") String jwt) throws UserException, OrderException {
        
        Usuario user = userService.findUserProfileByJwt(jwt);
        OrderRequest createdRequest = orderRequestService.createRequest(user.getId(), req.getOrderId(), req.getReason(), req.getType());
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<OrderRequest>> getUserRequests(
            @RequestHeader("Authorization") String jwt) throws UserException {
        
        Usuario user = userService.findUserProfileByJwt(jwt);
        List<OrderRequest> requests = orderRequestService.getUserRequests(user.getId());
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }
}
