package com.proyecto.response;

import java.util.List;

public class ProductCardResponse {
    private Long id;
    private String title;
    private String marca;
    private int price;             // Precio final (S/ 78.90)
    private int descuentoprice;    // Precio tachado (S/ 119.90)
    private int descuentot;        // Etiqueta roja (-25%)
    private boolean isNuevo;       // Etiqueta negra (NUEVO)
    private String mainImageUrl;   // La foto de portada
//    private int numRatings;        // Estrellitas (ej: 18)
    private List<String> availableColors; // Los circulitos de colores abajo



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



	public int getDescuentoprice() {
		return descuentoprice;
	}



	public void setDescuentoprice(int descuentoprice) {
		this.descuentoprice = descuentoprice;
	}



	public int getDescuentot() {
		return descuentot;
	}



	public void setDescuentot(int descuentot) {
		this.descuentot = descuentot;
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



	public ProductCardResponse() {}
}