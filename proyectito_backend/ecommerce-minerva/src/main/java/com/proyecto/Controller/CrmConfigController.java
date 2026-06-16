package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.model.CrmConfig;
import com.proyecto.repositories.CrmConfigRepository;

@RestController
@RequestMapping("/api/admin/crm")
public class CrmConfigController {

    @Autowired
    private CrmConfigRepository crmConfigRepository;

    @GetMapping("/config")
    public ResponseEntity<CrmConfig> getConfig() {
        CrmConfig config = crmConfigRepository.findAll().stream().findFirst().orElseGet(() -> {
            CrmConfig newConfig = new CrmConfig();
            return crmConfigRepository.save(newConfig);
        });
        return ResponseEntity.ok(config);
    }

    @PostMapping("/config")
    public ResponseEntity<CrmConfig> saveConfig(@RequestBody CrmConfig request) {
        CrmConfig config = crmConfigRepository.findAll().stream().findFirst().orElseGet(CrmConfig::new);
        config.setActive(request.isActive());
        config.setDaysWithoutPurchase(request.getDaysWithoutPurchase());
        config.setDiscountPercentage(request.getDiscountPercentage());
        return ResponseEntity.ok(crmConfigRepository.save(config));
    }

    @PostMapping("/test-trigger")
    public ResponseEntity<String> testTrigger() {
        try {
            com.proyecto.scheduler.ChurnNotificationScheduler scheduler = 
                org.springframework.web.context.support.WebApplicationContextUtils
                .getRequiredWebApplicationContext(
                    ((org.springframework.web.context.request.ServletRequestAttributes) 
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                    .getRequest().getServletContext()
                ).getBean(com.proyecto.scheduler.ChurnNotificationScheduler.class);
            
            scheduler.processChurnNotifications();
            return ResponseEntity.ok("Proceso de validación y envío de CRM ejecutado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al ejecutar el CRM: " + e.getMessage());
        }
    }
}
