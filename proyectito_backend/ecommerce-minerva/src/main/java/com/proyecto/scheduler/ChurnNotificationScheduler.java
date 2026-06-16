package com.proyecto.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.proyecto.model.Coupon;
import com.proyecto.model.CrmConfig;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.CouponRepository;
import com.proyecto.repositories.CrmConfigRepository;
import com.proyecto.repositories.UserRepository;
import com.proyecto.service.EmailService;

@Component
public class ChurnNotificationScheduler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CrmConfigRepository crmConfigRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CouponRepository couponRepository;

    // Se ejecuta todos los días a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void processChurnNotifications() {
        System.out.println("Running Churn Notification Scheduler...");
        
        CrmConfig config = crmConfigRepository.findAll().stream().findFirst().orElse(null);
        if (config == null || !config.isActive()) {
            System.out.println("CRM Notifications are disabled or not configured.");
            return;
        }

        LocalDateTime orderThreshold = LocalDateTime.now().minusDays(config.getDaysWithoutPurchase());
        LocalDateTime emailThreshold = LocalDateTime.now().minusDays(30); // Prevent spam, max 1 email per 30 days

        List<Usuario> churnCandidates = userRepository.findChurnCandidates(orderThreshold, emailThreshold);
        System.out.println("Found " + churnCandidates.size() + " users at risk of churn.");

        for (Usuario user : churnCandidates) {
            try {
                String couponCode = generateCoupon(user, config.getDiscountPercentage());
                emailService.sendReactivationEmail(user, couponCode, config.getDiscountPercentage());
            } catch (Exception e) {
                System.err.println("Error sending churn email for " + user.getEmail() + ": " + e.getMessage());
            } finally {
                // Update timestamp regardless to prevent generating multiple coupons if email fails continuously
                user.setLastReactivationEmailSent(LocalDateTime.now());
                userRepository.save(user);
            }
        }
    }

    private String generateCoupon(Usuario user, int discountPercentage) {
        if (discountPercentage <= 0) {
            return "N/A";
        }
        
        String code = "VUELVE-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setName("Cupón de Reactivación");
        coupon.setDiscount(discountPercentage);
        coupon.setType("percentage");
        coupon.setStartDate(LocalDateTime.now());
        coupon.setEndDate(LocalDateTime.now().plusDays(7)); // Válido por 7 días
        coupon.setUsageLimit(1);
        coupon.setUsageCount(0);
        coupon.setStatus("activo");
        coupon.setMinPurchase(0.0);
        coupon.setUser(user);
        
        couponRepository.save(coupon);
        return code;
    }
}
