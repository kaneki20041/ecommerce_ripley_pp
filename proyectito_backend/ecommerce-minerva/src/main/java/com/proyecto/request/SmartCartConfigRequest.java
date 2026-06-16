package com.proyecto.request;

public class SmartCartConfigRequest {

    private boolean isActive;
    private int upsellDiscountPercentage;
    private int maxProductsToShow;
    private String recommendationCriteria;
    private int minStockRequired;
    private String aiMetrics;

    public SmartCartConfigRequest() {}

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
