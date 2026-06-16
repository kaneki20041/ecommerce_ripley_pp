package com.proyecto.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.Coupon;
import com.proyecto.request.CouponRequest;
import com.proyecto.response.ApiResponse;
import com.proyecto.service.CouponService;

@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(@RequestBody CouponRequest req) throws ProductException {
        Coupon created = couponService.createCoupon(req);
        return new ResponseEntity<>(new ApiResponse<>("Cupón creado exitosamente", true, created), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Coupon>>> getAllCoupons() {
        List<Coupon> coupons = couponService.getAllCoupons();
        return new ResponseEntity<>(new ApiResponse<>("Lista de cupones", true, coupons), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Coupon>> updateCoupon(@PathVariable Long id, @RequestBody CouponRequest req) throws ProductException {
        Coupon updated = couponService.updateCoupon(id, req);
        return new ResponseEntity<>(new ApiResponse<>("Cupón actualizado", true, updated), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) throws ProductException {
        couponService.deleteCoupon(id);
        return new ResponseEntity<>(new ApiResponse<>("Cupón eliminado", true, null), HttpStatus.OK);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Coupon>> toggleCouponStatus(@PathVariable Long id) throws ProductException {
        Coupon updated = couponService.toggleCouponStatus(id);
        return new ResponseEntity<>(new ApiResponse<>("Estado de cupón cambiado", true, updated), HttpStatus.OK);
    }
}
