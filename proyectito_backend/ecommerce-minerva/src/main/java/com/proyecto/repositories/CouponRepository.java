package com.proyecto.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.model.Coupon;

import java.util.List;

import com.proyecto.model.Usuario;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    
    // Encuentra cupones que pertenecen al usuario o que son globales (user is null)
    List<Coupon> findByUserOrUserIsNull(Usuario user);
}
