package com.proyecto.response;

import com.proyecto.model.Product;
import com.proyecto.model.ProductVariant;

public class ProductAdminListResponse {

    private Long variantId;

    private String title;
    private String categoria;
    private Long categoriaId;
    private int price;
    private String estado;

    private String sku;
    private String color;
    private String size;
    private int stock;

    public ProductAdminListResponse(Product product, ProductVariant variant) {
        this.variantId = variant.getId();
        this.sku = variant.getSku() != null ? variant.getSku() : "";

        this.title = product.getTitle();
        this.categoria = product.getCategoria() != null ? product.getCategoria().getName() : "";
        this.categoriaId = product.getCategoria() != null ? product.getCategoria().getId() : null;

        // ✅ EL CAMBIO ESTÁ AQUÍ: Ahora el precio de la tabla lo dicta la variante, no el producto
        this.price = variant.getPrice();

        this.estado = variant.isActivo() ? "Activo" : "Inactivo";

        this.color = variant.getColor() != null ? variant.getColor().getNombre() : "";
        this.size = variant.getTalla() != null ? variant.getTalla().getValor() : "";
        this.stock = variant.getStock();
    }

	public Long getVariantId() {
		return variantId;
	}

	public void setVariantId(Long variantId) {
		this.variantId = variantId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}
}