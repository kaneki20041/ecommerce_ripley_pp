package com.proyecto.response;

import java.util.List;

public class ProductCardResponse {
    private Long id;
    private String title;
    private String marca;
    private int price;             // Precio final (S/ 78.90)
    private int discountedPrice;    // Precio tachado (S/ 119.90)
    private int discountPercent;        // Etiqueta roja (-25%)
    private boolean isNuevo;       // Etiqueta negra (NUEVO)
    private String mainImageUrl;   // La foto de portada
    private int stock;             // Stock total del producto
//    private int numRatings;        // Estrellitas (ej: 18)
    private List<String> availableColors; // Los circulitos de colores abajo
    private List<String> availableSizes;  // Tallas disponibles
    private String suggestedSize;         // Talla sugerida según carrito
    private String categoryName;          // Nombre de la categoría (para ignorarla)
    public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String getMarca() {
		return marca;
	}



	public void setMarca(String marca) {
		this.marca = marca;
	}



	public int getPrice() {
		return price;
	}



	public void setPrice(int price) {
		this.price = price;
	}



	public int getDiscountedPrice() {
		return discountedPrice;
	}



	public void setDiscountedPrice(int discountedPrice) {
		this.discountedPrice = discountedPrice;
	}



	public int getDiscountPercent() {
		return discountPercent;
	}



	public void setDiscountPercent(int discountPercent) {
		this.discountPercent = discountPercent;
	}



	public boolean isNuevo() {
		return isNuevo;
	}



	public void setNuevo(boolean isNuevo) {
		this.isNuevo = isNuevo;
	}



	public String getMainImageUrl() {
		return mainImageUrl;
	}



	public void setMainImageUrl(String mainImageUrl) {
		this.mainImageUrl = mainImageUrl;
	}



//	public int getNumRatings() {
//		return numRatings;
//	}
//
//
//
//	public void setNumRatings(int numRatings) {
//		this.numRatings = numRatings;
//	}



	public List<String> getAvailableColors() {
		return availableColors;
	}



	public void setAvailableColors(List<String> availableColors) {
		this.availableColors = availableColors;
	}



	public int getStock() {
		return stock;
	}



	public void setStock(int stock) {
		this.stock = stock;
	}



	public ProductCardResponse() {}

	public List<String> getAvailableSizes() {
		return availableSizes;
	}

	public void setAvailableSizes(List<String> availableSizes) {
		this.availableSizes = availableSizes;
	}

	public String getSuggestedSize() {
		return suggestedSize;
	}

	public void setSuggestedSize(String suggestedSize) {
		this.suggestedSize = suggestedSize;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
}