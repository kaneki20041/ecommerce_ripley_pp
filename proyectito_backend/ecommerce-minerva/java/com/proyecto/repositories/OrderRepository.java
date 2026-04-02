package com.proyecto.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.model.Order;

public interface OrderRepository extends JpaRepository<Order,Long>{

    @Query("SELECT o FROM Order o WHERE o.user.id = :id AND (o.orderStatus = 'PLACED' OR o.orderStatus = 'CONFIRMED' OR o.orderStatus = 'DELIVERED')")
    public List<Order> getUsersOrders(@Param("id") Long userId);
}
