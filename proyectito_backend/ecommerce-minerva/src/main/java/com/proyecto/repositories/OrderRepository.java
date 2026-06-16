package com.proyecto.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.model.Order;

public interface OrderRepository extends JpaRepository<Order,Long>{

    @Query("SELECT o FROM Order o WHERE o.user.id = :id")
    public List<Order> getUsersOrders(@Param("id") Long userId);

    @Query("SELECT COALESCE(SUM(CASE WHEN o.totalDiscountedPrice > 0 THEN o.totalDiscountedPrice ELSE o.totalPrice END), 0) " +
           "FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate AND o.orderStatus IN ('DELIVERED', 'ENTREGADO', 'COMPLETADO', 'PICKED_UP', 'PAGADO')")
    public Double sumTotalRevenueByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate AND o.orderStatus IN ('DELIVERED', 'ENTREGADO', 'COMPLETADO', 'PICKED_UP', 'PAGADO')")
    public Long countOrdersByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(o.totalItem), 0) FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate AND o.orderStatus IN ('DELIVERED', 'ENTREGADO', 'COMPLETADO', 'PICKED_UP', 'PAGADO')")
    public Long sumProductsSoldByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate ORDER BY o.orderDate DESC")
    public List<Order> findByOrderDateBetween(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
}
