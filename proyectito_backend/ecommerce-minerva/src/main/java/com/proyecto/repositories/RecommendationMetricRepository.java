package com.proyecto.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.proyecto.model.RecommendationMetric;

public interface RecommendationMetricRepository extends JpaRepository<RecommendationMetric, Long> {
    @Query("SELECT m FROM RecommendationMetric m WHERE m.product.id = :productId")
    Optional<RecommendationMetric> findByProductIdCustom(@Param("productId") Long productId);

    @Query("SELECT SUM(m.impressions) FROM RecommendationMetric m")
    Long sumTotalImpressions();

    @Query("SELECT SUM(m.clicks) FROM RecommendationMetric m")
    Long sumTotalClicks();
}
