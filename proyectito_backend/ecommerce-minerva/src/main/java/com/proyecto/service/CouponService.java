package com.proyecto.service;

import java.util.List;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.Coupon;
import com.proyecto.request.CouponRequest;

public interface CouponService {
    Coupon createCoupon(CouponRequest req) throws ProductException;
    Coupon updateCoupon(Long id, CouponRequest req) throws ProductException;
    void deleteCoupon(Long id) throws ProductException;
    List<Coupon> getAllCoupons();
    List<Coupon> getCouponsByUser(com.proyecto.model.Usuario user);
    Coupon getCouponByCode(String code) throws ProductException;

    Coupon toggleCouponStatus(Long id) throws ProductException;
    boolean validateCoupon(String code, double cartTotal) throws ProductException;
    void incrementUsage(String code) throws ProductException;
}
