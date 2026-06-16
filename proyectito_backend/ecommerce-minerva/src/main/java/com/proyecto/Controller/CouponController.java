package com.proyecto.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.proyecto.Exception.ProductException;
import com.proyecto.Exception.UserException;
import com.proyecto.model.Coupon;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.UserRepository;
import com.proyecto.response.ApiResponse;
import com.proyecto.response.ApiResponse;
import com.proyecto.service.CouponService;
import com.proyecto.repositories.CouponRepository;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    public CouponController(CouponService couponService, UserRepository userRepository, CouponRepository couponRepository) {
        this.couponService = couponService;
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<ApiResponse<Coupon>> validateCoupon(
            @PathVariable String code,
            @RequestParam(required = false, defaultValue = "0") double cartTotal) throws ProductException {
        
        couponService.validateCoupon(code, cartTotal);
        Coupon coupon = couponService.getCouponByCode(code);
        
        return new ResponseEntity<>(new ApiResponse<>("Cupón válido", true, coupon), HttpStatus.OK);
    }

    @GetMapping("/my-coupons")
    public ResponseEntity<ApiResponse<List<Coupon>>> getMyCoupons() throws UserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        Usuario user = userRepository.findByEmail(email);
        
        List<Coupon> coupons = couponService.getCouponsByUser(user);
        return new ResponseEntity<>(new ApiResponse<>("Cupones obtenidos exitosamente", true, coupons), HttpStatus.OK);
    }

    @PostMapping("/migrate-welcome")
    public ResponseEntity<ApiResponse<String>> migrateWelcomeCoupons() {
        List<Usuario> allUsers = userRepository.findAll();
        int added = 0;
        for (Usuario user : allUsers) {
            // Check if user already has a welcome coupon
            List<Coupon> userCoupons = couponService.getCouponsByUser(user);
            boolean hasWelcome = userCoupons.stream().anyMatch(c -> "Primera Compra".equals(c.getName()));
            
            if (!hasWelcome) {
                Coupon coupon = new Coupon();
                coupon.setName("Primera Compra");
                coupon.setCode("BIENVENIDO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                coupon.setDiscount(40);
                coupon.setType("percentage");
                coupon.setStartDate(LocalDateTime.now());
                coupon.setEndDate(LocalDateTime.now().plusDays(30));
                coupon.setStatus("activo");
                coupon.setUser(user);
                coupon.setUpsellDefault(false);
                couponRepository.save(coupon);
                added++;
            }
        }
        return new ResponseEntity<>(new ApiResponse<>("Cupones de bienvenida migrados para " + added + " usuarios.", true, null), HttpStatus.OK);
    }
}
