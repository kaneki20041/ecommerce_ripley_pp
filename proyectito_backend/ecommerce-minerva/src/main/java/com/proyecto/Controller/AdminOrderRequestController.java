package com.proyecto.Controller;

import com.proyecto.model.OrderRequest;
import com.proyecto.request.UpdateOrderRequestStatusDto;
import com.proyecto.service.OrderRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/requests")
public class AdminOrderRequestController {

    @Autowired
    private OrderRequestService orderRequestService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderRequest>> getAllRequests() {
        List<OrderRequest> requests = orderRequestService.getAllRequests();
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<OrderRequest> updateRequestStatus(
            @PathVariable Long requestId,
            @RequestBody UpdateOrderRequestStatusDto req) throws Exception {
        
        OrderRequest updatedRequest = orderRequestService.updateRequestStatus(requestId, req.getStatus());
        return new ResponseEntity<>(updatedRequest, HttpStatus.OK);
    }
}
