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
    // Usamos JOIN v para buscar dentro de las variantes
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.variantes v WHERE " +
            "(:categoria IS NULL OR p.categoria.name = :categoria) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:minDiscount IS NULL OR p.descuentot >= :minDiscount) " +
            "AND (COALESCE(:colors, NULL) IS NULL OR LOWER(v.color) IN :colors) " +
            "AND (COALESCE(:sizes, NULL) IS NULL OR v.size IN :sizes) " +
            "AND (:stock = 'en_stock' AND v.stock > 0 OR " +
            ":stock = 'sin_stock' AND v.stock < 1 OR " +
            ":stock IS NULL) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'precio_menor' THEN p.price END ASC, " +
            "CASE WHEN :sort = 'precio_mayor' THEN p.price END DESC")
    Page<Product> filterProducts(
            @Param("categoria") String categoria,
            @Param("colors") List<String> colors,
            @Param("sizes") List<String> sizes,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("minDiscount") Integer minDiscount,
            @Param("sort") String sort,
            @Param("stock") String stock,
            Pageable pageable
    );
}