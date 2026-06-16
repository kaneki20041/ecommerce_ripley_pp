package com.proyecto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "smart_cart_config")
public class SmartCartConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isActive;
    private int upsellDiscountPercentage;
    private int maxProductsToShow;
    private String recommendationCriteria;
    private int minStockRequired;

    @jakarta.persistence.Column(name = "ai_metrics", length = 500)
    private String aiMetrics;

    public SmartCartConfig() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getUpsellDiscountPercentage() {
        return upsellDiscountPercentage;
    }

    public void setUpsellDiscountPercentage(int upsellDiscountPercentage) {
        this.upsellDiscountPercentage = upsellDiscountPercentage;
    }

    public int getMaxProductsToShow() {
        return maxProductsToShow;
    }

    public void setMaxProductsToShow(int maxProductsToShow) {
        this.maxProductsToShow = maxProductsToShow;
    }

    public String getRecommendationCriteria() {
        return recommendationCriteria;
    }

    public void setRecommendationCriteria(String recommendationCriteria) {
        this.recommendationCriteria = recommendationCriteria;
    }

    public int getMinStockRequired() {
        return minStockRequired;
    }

    public void setMinStockRequired(int minStockRequired) {
        this.minStockRequired = minStockRequired;
    }

    public String getAiMetrics() {
        return aiMetrics;
    }

    public void setAiMetrics(String aiMetrics) {
        this.aiMetrics = aiMetrics;
    }
}
