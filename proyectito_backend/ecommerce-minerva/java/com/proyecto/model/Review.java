package com.proyecto.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Review {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	private String review;

	@ManyToOne
	@JoinColumn(name="Product_id")
	@JsonIgnore
	private Product product;

	@ManyToOne
	@JoinColumn(name="Usuario_id")
	private Usuario usuario;

	private LocalDateTime createdAt;

	public Review() {

	}
	public Review(Long id, String review, Product product, Usuario usuario, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.review = review;
		this.product = product;
		this.usuario = usuario;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}


}
