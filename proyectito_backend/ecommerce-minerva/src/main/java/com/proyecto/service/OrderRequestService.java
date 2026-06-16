package com.proyecto.service;

import com.proyecto.Exception.OrderException;
import com.proyecto.model.OrderRequest;

import java.util.List;

public interface OrderRequestService {

    OrderRequest createRequest(Long userId, Long orderId, String reason, String type) throws OrderException;

    OrderRequest updateRequestStatus(Long requestId, String status) throws Exception;

    List<OrderRequest> getUserRequests(Long userId);

    List<OrderRequest> getAllRequests();
    
    OrderRequest getRequestById(Long requestId) throws Exception;
}
