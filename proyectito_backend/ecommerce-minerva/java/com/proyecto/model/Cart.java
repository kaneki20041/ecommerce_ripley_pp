package com.proyecto.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;


@Entity
public class Cart {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne (fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private Usuario user;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	@Column (name = "cart_items")
	private Set<CartItem> cartitems = new HashSet<>();

	@Column (name = "total_price")
	private double totalPrice;

	@Column(name="total_item")
	private int totalitem;

	private int totalDiscountedPrice;

	private int discounte;


	public Cart() {

	}



	public Cart(Long id, Usuario user, Set<CartItem> cartitems, double totalPrice, int totalitem,
			int totalDiscountedPrice, int discounte) {
		super();
		this.id = id;
		this.user = user;
		this.cartitems = cartitems;
		this.totalPrice = totalPrice;
		this.totalitem = totalitem;
		this.totalDiscountedPrice = totalDiscountedPrice;
		this.discounte = discounte;
	}



	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public Usuario getUser() {
		return user;
	}



	public void setUser(Usuario user) {
		this.user = user;
	}



	public Set<CartItem> getCartitems() {
		return cartitems;
	}



	public void setCartitems(Set<CartItem> cartitems) {
		this.cartitems = cartitems;
	}



	public double getTotalPrice() {
		return totalPrice;
	}



	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}



	public int getTotalitem() {
		return totalitem;
	}



	public void setTotalitem(int totalitem) {
		this.totalitem = totalitem;
	}



	public int getTotalDiscountedPrice() {
		return totalDiscountedPrice;
	}



	public void setTotalDiscountedPrice(int totalDiscountedPrice) {
		this.totalDiscountedPrice = totalDiscountedPrice;
	}



	public int getDiscounte() {
		return discounte;
	}



	public void setDiscounte(int discounte) {
		this.discounte = discounte;
	}


}
