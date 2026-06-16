package com.proyecto.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.variantes LEFT JOIN FETCH p.categoria")
    List<Product> findAllProductsOptimized();
	
	@Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.variantes v WHERE p.isDestacado = true")
    Page<Product> findFeaturedProducts(Pageable pageable);

	@Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.variantes v WHERE p.isNuevo = true")
    Page<Product> findNewProducts(Pageable pageable);
	@Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.variantes v WHERE " +
            "(:categoria IS NULL OR p.categoria.name = :categoria) " +
            "AND (:genero IS NULL OR p.genero = :genero OR p.genero = 'Unisex') " +
            "AND (:isNuevo IS NULL OR p.isNuevo = :isNuevo) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:minDiscount IS NULL OR p.discountPercent >= :minDiscount) " +
            "AND (COALESCE(:colors, NULL) IS NULL OR LOWER(v.color.nombre) IN :colors) " +
            "AND (COALESCE(:sizes, NULL) IS NULL OR v.talla.valor IN :sizes) " +
            "AND (:stock IS NULL OR (:stock = 'en_stock' AND v.stock > 0) OR (:stock = 'sin_stock' AND v.stock < 1))")
    Page<Product> filterProducts(
            @Param("categoria") String categoria,
            @Param("genero") String genero,
            @Param("isNuevo") Boolean isNuevo,
            @Param("colors") List<String> colors,
            @Param("sizes") List<String> sizes,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("minDiscount") Integer minDiscount,
            @Param("stock") String stock,
            Pageable pageable // Ya no pasamos el "sort" como String al Query
    );

	@Query("SELECT p FROM Product p WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate ORDER BY p.createdAt DESC")
    List<Product> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
}