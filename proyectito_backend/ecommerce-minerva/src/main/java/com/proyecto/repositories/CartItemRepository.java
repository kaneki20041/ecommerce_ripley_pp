package com.proyecto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.model.Cart;
import com.proyecto.model.CartItem;
import com.proyecto.model.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.product = :product AND ci.size = :size AND ((ci.color IS NULL AND :color IS NULL) OR ci.color = :color) AND ci.userId = :userId")
    public CartItem isCartItemExist(@Param("cart") Cart cart, @Param("product") Product product,
                                    @Param("size") String size, @Param("color") String color, @Param("userId") Long userId);
}

