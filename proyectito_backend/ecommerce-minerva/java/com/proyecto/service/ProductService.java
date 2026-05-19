package com.proyecto.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.Product;
import com.proyecto.model.ProductVariant;
import com.proyecto.model.Usuario;
import com.proyecto.request.CreateProductRequest;
import com.proyecto.request.UpdateProductBasicRequest;
import com.proyecto.request.UpdateVariantRequest;
import com.proyecto.response.PaginatedResponse;
import com.proyecto.response.ProductAdminListResponse;
import com.proyecto.response.ProductCardResponse;

public interface ProductService {
	public Product createProduct(CreateProductRequest req, Usuario creador);

	//public Product updateProduct(Long productId, CreateProductRequest req) throws ProductException;

	public Product findProductById(Long id) throws ProductException;

	public String toggleVariantStatus(Long variantId) throws ProductException;

	public PaginatedResponse<ProductCardResponse>getAllProductPublic(String categoria, String genero, Boolean isNuevo, 
            List<String> colors, List<String> sizes,
            Integer minPrice, Integer maxPrice, Integer minDiscount,
            String sort, String stock, Integer pageNumber, Integer pageSize);

	public List<Product> findAllProducts();

	public ProductVariant getVariantById(Long variantId) throws ProductException;

	public ProductVariant updateVariant(Long variantId, UpdateVariantRequest req) throws ProductException;

	Product updateProductBasicInfo(Long productId, UpdateProductBasicRequest req) throws ProductException;
//	void generateSampleProducts(int numberOfProducts);
	public PaginatedResponse<ProductAdminListResponse> getPaginatedVariants(int pageNumber, int pageSize);
}
