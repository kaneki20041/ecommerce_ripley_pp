package com.proyecto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long>{

}
