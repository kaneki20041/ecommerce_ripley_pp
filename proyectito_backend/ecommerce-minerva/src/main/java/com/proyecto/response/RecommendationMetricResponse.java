package com.proyecto.response;

import com.proyecto.model.RecommendationMetric;

public class RecommendationMetricResponse {
    private Long id;
    private Long productId;
    private int impressions;
    private int clicks;

    public RecommendationMetricResponse() {}

    public RecommendationMetricResponse(RecommendationMetric metric) {
        if (metric != null) {
            this.id = metric.getId();
            this.productId = metric.getProduct() != null ? metric.getProduct().getId() : null;
            this.impressions = metric.getImpressions();
            this.clicks = metric.getClicks();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getImpressions() { return impressions; }
    public void setImpressions(int impressions) { this.impressions = impressions; }
    public int getClicks() { return clicks; }
    public void setClicks(int clicks) { this.clicks = clicks; }
}
