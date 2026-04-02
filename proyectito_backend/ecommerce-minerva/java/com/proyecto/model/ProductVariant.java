package com.proyecto.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ProductVariant {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    // A qué producto pertenece esta variante
    @ManyToOne
    @JoinColumn(name="product_id", nullable=false)
    @JsonIgnore
    private Product product;
	private int descuentot;
    private String color;
    private String size;
    @ElementCollection
    @CollectionTable(name="variant_images", joinColumns=@JoinColumn(name="variant_id"))
    @Column(name="image_url", columnDefinition = "VARCHAR(MAX)")
    private List<String> imageUrls = new ArrayList<>();

    private int stock;
    @Column(nullable = false)
    private boolean activo = true;
    // Opcional: Si la talla XXL cuesta más, puedes poner un precio aquí
    // private int precioExtra;

    private int price;
    private int descuentoprice;

    private LocalDateTime fechaInicioDescuento;
    private LocalDateTime fechaFinDescuento;

    public ProductVariant() {}

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Product getProduct() {
		return product;
	}


	public void setProduct(Product product) {
		this.product = product;
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

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public List<String> getImageUrls() {
		return imageUrls;
	}

	public void setImageUrls(List<String> imageUrls) {
		this.imageUrls = imageUrls;
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

	public LocalDateTime getFechaInicioDescuento() {
		return fechaInicioDescuento;
	}

	public void setFechaInicioDescuento(LocalDateTime fechaInicioDescuento) {
		this.fechaInicioDescuento = fechaInicioDescuento;
	}

	public LocalDateTime getFechaFinDescuento() {
		return fechaFinDescuento;
	}

	public void setFechaFinDescuento(LocalDateTime fechaFinDescuento) {
		this.fechaFinDescuento = fechaFinDescuento;
	}

	public int getDescuentot() {
		return descuentot;
	}

	public void setDescuentot(int descuentot) {
		this.descuentot = descuentot;
	}


}