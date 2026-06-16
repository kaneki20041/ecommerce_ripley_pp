package com.proyecto.repositories;

import com.proyecto.model.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {

    @Query("SELECT r FROM OrderRequest r WHERE r.user.id = :userId")
    List<OrderRequest> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM OrderRequest r WHERE r.order.id = :orderId")
    List<OrderRequest> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT r FROM OrderRequest r WHERE r.status = :status")
    List<OrderRequest> findByStatus(@Param("status") String status);
}
