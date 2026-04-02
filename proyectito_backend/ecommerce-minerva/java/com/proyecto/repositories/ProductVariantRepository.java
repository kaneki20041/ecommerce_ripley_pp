package com.proyecto.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.model.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

	@Query(value = "SELECT v FROM ProductVariant v JOIN FETCH v.product",
	           countQuery = "SELECT count(v) FROM ProductVariant v")
	    Page<ProductVariant> findAllVariantsWithProduct(Pageable pageable);
}