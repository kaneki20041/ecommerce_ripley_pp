package com.proyecto.service;

import com.proyecto.Exception.OrderException;
import com.proyecto.model.Order;
import com.proyecto.model.OrderRequest;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.OrderRepository;
import com.proyecto.repositories.OrderRequestRepository;
import com.proyecto.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderRequestServiceImplementation implements OrderRequestService {

    @Autowired
    private OrderRequestRepository orderRequestRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public OrderRequest createRequest(Long userId, Long orderId, String reason, String type) throws OrderException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Pedido no encontrado con ID: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new OrderException("El usuario no tiene permiso para solicitar acciones sobre este pedido");
        }

        Usuario user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderException("Usuario no encontrado con ID: " + userId));

        // Validar reglas de negocio según tipo de solicitud
        if ("CANCELACION".equalsIgnoreCase(type)) {
            if (!"PENDING".equalsIgnoreCase(order.getOrderStatus()) && !"PLACED".equalsIgnoreCase(order.getOrderStatus())) {
                throw new OrderException("Solo se pueden solicitar cancelaciones para pedidos PENDIENTES.");
            }
        } else if ("DEVOLUCION".equalsIgnoreCase(type)) {
            if (!"DELIVERED".equalsIgnoreCase(order.getOrderStatus())) {
                throw new OrderException("Solo se pueden solicitar devoluciones para pedidos ENTREGADOS.");
            }
        } else {
            throw new OrderException("Tipo de solicitud inválido.");
        }

        // Verificar si ya existe una solicitud en proceso
        List<OrderRequest> existingRequests = orderRequestRepository.findByOrderId(orderId);
        boolean hasPending = existingRequests.stream()
                .anyMatch(req -> "PENDIENTE".equalsIgnoreCase(req.getStatus()));

        if (hasPending) {
            throw new OrderException("Ya existe una solicitud pendiente para este pedido.");
        }

        OrderRequest request = new OrderRequest(order, user, type.toUpperCase(), "PENDIENTE", reason, LocalDateTime.now());
        return orderRequestRepository.save(request);
    }

    @Override
    public OrderRequest updateRequestStatus(Long requestId, String status) throws Exception {
        OrderRequest request = getRequestById(requestId);
        
        request.setStatus(status.toUpperCase());
        request.setResolutionDate(LocalDateTime.now());
        
        // Si es aprobada, actualizar el estado del pedido asociado
        if ("APROBADA".equalsIgnoreCase(status)) {
            Order order = request.getOrder();
            if ("CANCELACION".equalsIgnoreCase(request.getType())) {
                order.setOrderStatus("CANCELLED");
            } else if ("DEVOLUCION".equalsIgnoreCase(request.getType())) {
                order.setOrderStatus("RETURNED");
            }
            orderRepository.save(order);
        }

        return orderRequestRepository.save(request);
    }

    @Override
    public List<OrderRequest> getUserRequests(Long userId) {
        return orderRequestRepository.findByUserId(userId);
    }

    @Override
    public List<OrderRequest> getAllRequests() {
        return orderRequestRepository.findAll();
    }

    @Override
    public OrderRequest getRequestById(Long requestId) throws Exception {
        return orderRequestRepository.findById(requestId)
                .orElseThrow(() -> new Exception("Solicitud no encontrada con ID: " + requestId));
    }
}
