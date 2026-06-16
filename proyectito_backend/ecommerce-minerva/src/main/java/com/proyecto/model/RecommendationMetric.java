package com.proyecto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recommendation_metric", indexes = {
    @Index(name = "idx_metric_product_id", columnList = "productId")
})
public class RecommendationMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    private int impressions = 0;
    private int clicks = 0;

    public RecommendationMetric() {}

    public RecommendationMetric(Product product) {
        this.product = product;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getImpressions() { return impressions; }
    public void setImpressions(int impressions) { this.impressions = impressions; }
    public int getClicks() { return clicks; }
    public void setClicks(int clicks) { this.clicks = clicks; }
}
