package com.proyecto.service;

import org.springframework.stereotype.Service;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.SmartCartConfig;
import com.proyecto.repositories.SmartCartConfigRepository;
import com.proyecto.request.SmartCartConfigRequest;

@Service
public class SmartCartConfigServiceImplementation implements SmartCartConfigService {

    private final SmartCartConfigRepository smartCartConfigRepository;

    public SmartCartConfigServiceImplementation(SmartCartConfigRepository smartCartConfigRepository) {
        this.smartCartConfigRepository = smartCartConfigRepository;
    }

    @Override
    public SmartCartConfig getConfig() {
        return smartCartConfigRepository.findAll().stream().findFirst().orElseGet(() -> {
            // Should not happen as DataInitializer seeds it, but just in case
            SmartCartConfig defaultConfig = new SmartCartConfig();
            defaultConfig.setActive(true);
            defaultConfig.setUpsellDiscountPercentage(10);
            defaultConfig.setMaxProductsToShow(4);
            defaultConfig.setRecommendationCriteria("MOST_SOLD");
            defaultConfig.setMinStockRequired(3);
            defaultConfig.setAiMetrics("USER_HISTORY,CTR_DATA");
            return smartCartConfigRepository.save(defaultConfig);
        });
    }

    @Override
    public SmartCartConfig updateConfig(SmartCartConfigRequest req) throws ProductException {
        System.out.println("DEBUG - updateConfig API Call");
        System.out.println("DEBUG - isActive: " + req.isActive());
        System.out.println("DEBUG - upsellDiscountPercentage: " + req.getUpsellDiscountPercentage());
        System.out.println("DEBUG - aiMetrics: " + req.getAiMetrics());

        SmartCartConfig config = getConfig();
        
        config.setActive(req.isActive());
        config.setUpsellDiscountPercentage(req.getUpsellDiscountPercentage());
        config.setMaxProductsToShow(req.getMaxProductsToShow());
        config.setRecommendationCriteria(req.getRecommendationCriteria());
        config.setMinStockRequired(req.getMinStockRequired());
        if (req.getAiMetrics() != null) {
            config.setAiMetrics(req.getAiMetrics());
        }
        
        return smartCartConfigRepository.save(config);
    }
}
