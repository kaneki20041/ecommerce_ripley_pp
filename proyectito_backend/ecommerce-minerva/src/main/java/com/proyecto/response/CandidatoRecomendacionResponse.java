package com.proyecto.response;

import lombok.Data;

@Data
public class CandidatoRecomendacionResponse {
	private Long id;
    private String title;
    private String categoria;
    private String marca;
    private int price;
    private double ctr;
    private boolean isNuevo;
    private int discountPercent;
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
	public String getCategoria() {
		return categoria;
	}
	public void setCategoria(String categoria) {
		this.categoria = categoria;
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
	public double getCtr() {
		return ctr;
	}
	public void setCtr(double ctr) {
		this.ctr = ctr;
	}
	public boolean isNuevo() {
		return isNuevo;
	}
	public void setNuevo(boolean nuevo) {
		this.isNuevo = nuevo;
	}
	public int getDiscountPercent() {
		return discountPercent;
	}
	public void setDiscountPercent(int discountPercent) {
		this.discountPercent = discountPercent;
	}
}
