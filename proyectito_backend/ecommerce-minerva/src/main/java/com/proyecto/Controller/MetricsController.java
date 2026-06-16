package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.model.RecommendationMetric;
import com.proyecto.repositories.RecommendationMetricRepository;
import com.proyecto.response.ApiResponse;
import java.util.Optional;
import com.proyecto.response.RecommendationMetricResponse;
import com.proyecto.model.Product;
import com.proyecto.repositories.ProductRepository;

@RestController
@RequestMapping("/api/metrics/recommendations")
public class MetricsController {

    @Autowired
    private RecommendationMetricRepository metricRepository;

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/{productId}/impression")
    public ResponseEntity<ApiResponse<RecommendationMetricResponse>> trackImpression(@PathVariable Long productId) {
        RecommendationMetric metric = getOrCreateMetric(productId);
        metric.setImpressions(metric.getImpressions() + 1);
        RecommendationMetric saved = metricRepository.save(metric);
        RecommendationMetricResponse resDto = new RecommendationMetricResponse(saved);
        ApiResponse<RecommendationMetricResponse> res = new ApiResponse<>("Impresión registrada", true, resDto);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/{productId}/click")
    public ResponseEntity<ApiResponse<RecommendationMetricResponse>> trackClick(@PathVariable Long productId) {
        RecommendationMetric metric = getOrCreateMetric(productId);
        metric.setClicks(metric.getClicks() + 1);
        RecommendationMetric saved = metricRepository.save(metric);
        RecommendationMetricResponse resDto = new RecommendationMetricResponse(saved);
        ApiResponse<RecommendationMetricResponse> res = new ApiResponse<>("Clic registrado", true, resDto);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private RecommendationMetric getOrCreateMetric(Long productId) {
        Optional<RecommendationMetric> opt = metricRepository.findByProductIdCustom(productId);
        if (opt.isPresent()) {
            return opt.get();
        } else {
            Product p = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
            return new RecommendationMetric(p);
        }
    }
}
