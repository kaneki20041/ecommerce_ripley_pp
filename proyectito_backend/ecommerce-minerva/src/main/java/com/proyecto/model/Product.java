package com.proyecto.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Product {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	private String title;

	@Column(length=2500)
	private String description;

	private String genero;

    private String material;

    //precio actual
	private int price;

	//precio anterior
	private int discountedPrice;

	//porcentaje de descuento
	private int discountPercent;

	private String marca;

	@Column(name="num_ratings")
	private int numRatings;

	@ManyToOne()
	@JoinColumn(name="category_id")
	private Categoria categoria;

	private LocalDateTime createdAt;

	private boolean isNuevo;
    private boolean isDestacado;

	// Un producto tiene muchas variantes (combinaciones de talla/color)
    @OneToMany(mappedBy="product", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<ProductVariant> variantes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    @JsonIgnore // Para que no traiga toda la info del admin cuando pidas productos
    private Usuario creadoPor;

    private LocalDateTime fechaInicioDescuento;
    private LocalDateTime fechaFinDescuento;
	public Product() {}



	public Product(Long id, String title, String description, String genero, String material, int price,
			int discountedPrice, int discountPercent, String marca, int numRatings, Categoria categoria, LocalDateTime createdAt, boolean isNuevo,
			boolean isDestacado, List<ProductVariant> variantes, Usuario creadoPor) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.genero = genero;
		this.material = material;
		this.price = price;
		this.discountedPrice = discountedPrice;
		this.discountPercent = discountPercent;
		this.marca = marca;
		this.numRatings = numRatings;
		this.categoria = categoria;
		this.createdAt = createdAt;
		this.isNuevo = isNuevo;
		this.isDestacado = isDestacado;
		this.variantes = variantes;
		this.creadoPor = creadoPor;
	}



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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getPrice() {
		if (variantes != null && !variantes.isEmpty()) {
			for (ProductVariant v : variantes) {
				if (v != null && v.isActivo()) return v.getPrice();
			}
			return variantes.get(0).getPrice();
		}
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getDiscountedPrice() {
		if (variantes != null && !variantes.isEmpty()) {
			for (ProductVariant v : variantes) {
				if (v != null && v.isActivo()) return v.getDiscountedPrice();
			}
			return variantes.get(0).getDiscountedPrice();
		}
		return discountedPrice;
	}

	public void setDiscountedPrice(int discountedPrice) {
		this.discountedPrice = discountedPrice;
	}

	public int getDiscountPercent() {
		if (variantes != null && !variantes.isEmpty()) {
			for (ProductVariant v : variantes) {
				if (v != null && v.isActivo()) return v.getDiscountPercent();
			}
			return variantes.get(0).getDiscountPercent();
		}
		return discountPercent;
	}

	public void setDiscountPercent(int discountPercent) {
		this.discountPercent = discountPercent;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public int getNumRatings() {
		return numRatings;
	}

	public void setNumRatings(int numRatings) {
		this.numRatings = numRatings;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getGenero() {
		return genero;
	}

	public void setGenero(String genero) {
		this.genero = genero;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public List<ProductVariant> getVariantes() {
		return variantes;
	}

	public void setVariantes(List<ProductVariant> variantes) {
		this.variantes = variantes;
	}

	public boolean isNuevo() {
		return isNuevo;
	}

	public void setNuevo(boolean isNuevo) {
		this.isNuevo = isNuevo;
	}

	public boolean isDestacado() {
		return isDestacado;
	}

	public void setDestacado(boolean isDestacado) {
		this.isDestacado = isDestacado;
	}

	public Usuario getCreadoPor() {
		return creadoPor;
	}

	public void setCreadoPor(Usuario creadoPor) {
		this.creadoPor = creadoPor;
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
}

