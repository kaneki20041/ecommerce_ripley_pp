package com.proyecto.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.Coupon;
import com.proyecto.repositories.CouponRepository;
import com.proyecto.request.CouponRequest;

@Service
public class CouponServiceImplementation implements CouponService {

    private final CouponRepository couponRepository;

    public CouponServiceImplementation(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public Coupon createCoupon(CouponRequest req) throws ProductException {
        Optional<Coupon> opt = couponRepository.findByCode(req.getCode().toUpperCase());
        if (opt.isPresent()) {
            throw new ProductException("Coupon with code " + req.getCode() + " already exists.");
        }



        Coupon coupon = new Coupon();
        coupon.setCode(req.getCode().toUpperCase());
        coupon.setName(req.getName());
        coupon.setDiscount(req.getDiscount());
        coupon.setType(req.getType());
        coupon.setStartDate(req.getStartDate() != null ? req.getStartDate().atStartOfDay() : null);
        coupon.setEndDate(req.getEndDate() != null ? req.getEndDate().atTime(23, 59, 59) : null);
        coupon.setUsageLimit(req.getUsageLimit());
        coupon.setStatus(req.getStatus() != null ? req.getStatus() : "activo");
        coupon.setMinPurchase(req.getMinPurchase());
        coupon.setUsageCount(0);

        return couponRepository.save(coupon);
    }

    @Override
    public Coupon updateCoupon(Long id, CouponRequest req) throws ProductException {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ProductException("Coupon not found with id " + id));



        coupon.setCode(req.getCode().toUpperCase());
        coupon.setName(req.getName());
        coupon.setDiscount(req.getDiscount());
        coupon.setType(req.getType());
        coupon.setStartDate(req.getStartDate() != null ? req.getStartDate().atStartOfDay() : null);
        coupon.setEndDate(req.getEndDate() != null ? req.getEndDate().atTime(23, 59, 59) : null);
        coupon.setUsageLimit(req.getUsageLimit());
        if (req.getStatus() != null) {
            coupon.setStatus(req.getStatus());
        }
        coupon.setMinPurchase(req.getMinPurchase());

        return couponRepository.save(coupon);
    }



    @Override
    public void deleteCoupon(Long id) throws ProductException {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ProductException("Coupon not found with id " + id));
        couponRepository.delete(coupon);
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public List<Coupon> getCouponsByUser(com.proyecto.model.Usuario user) {
        return couponRepository.findByUserOrUserIsNull(user);
    }

    @Override
    public Coupon getCouponByCode(String code) throws ProductException {
        return couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ProductException("Coupon not found with code " + code));
    }



    @Override
    public Coupon toggleCouponStatus(Long id) throws ProductException {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ProductException("Coupon not found with id " + id));
        
        if ("activo".equalsIgnoreCase(coupon.getStatus())) {
            coupon.setStatus("inactivo");
        } else {
            coupon.setStatus("activo");
        }
        return couponRepository.save(coupon);
    }

    @Override
    public boolean validateCoupon(String code, double cartTotal) throws ProductException {
        Coupon coupon = getCouponByCode(code);
        
        if (!"activo".equalsIgnoreCase(coupon.getStatus())) {
            throw new ProductException("El cupón no está activo.");
        }
        
        if (coupon.getEndDate() != null && LocalDateTime.now().isAfter(coupon.getEndDate())) {
            coupon.setStatus("expirado");
            couponRepository.save(coupon);
            throw new ProductException("El cupón ha expirado.");
        }
        
        if (coupon.getStartDate() != null && LocalDateTime.now().isBefore(coupon.getStartDate())) {
            throw new ProductException("El cupón aún no es válido.");
        }
        
        if (coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new ProductException("El cupón ha alcanzado su límite de usos.");
        }
        
        if (cartTotal < coupon.getMinPurchase()) {
            throw new ProductException("Se requiere una compra mínima de S/ " + coupon.getMinPurchase() + " para usar este cupón.");
        }
        
        return true;
    }

    @Override
    public void incrementUsage(String code) throws ProductException {
        Coupon coupon = getCouponByCode(code);
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);
    }
}
