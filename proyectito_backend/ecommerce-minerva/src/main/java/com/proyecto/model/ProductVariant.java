package com.proyecto.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variant")
@Data
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;
    
    @Column(nullable = false, unique = true, length = 8)
    private String sku;

    // ✅ RELACIONES MAESTRAS: Ahora la base de datos indexa numéricamente
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "talla_id", nullable = false)
    private Talla talla;

    private int discountPercent;
    private int stock;
    private boolean activo = true;
    private int price;
    private int discountedPrice;

    public ProductVariant() {
	}
	public ProductVariant(Long id, Product product, String sku, Color color, Talla talla, int discountPercent, int stock, boolean activo,
			int price, int discountedPrice, List<String> imageUrls, LocalDateTime fechaInicioDescuento,
			LocalDateTime fechaFinDescuento) {
		super();
		this.id = id;
		this.product = product;
		this.sku = sku;
		this.color = color;
		this.talla = talla;
		this.discountPercent = discountPercent;
		this.stock = stock;
		this.activo = activo;
		this.price = price;
		this.discountedPrice = discountedPrice;
		this.imageUrls = imageUrls;
		this.fechaInicioDescuento = fechaInicioDescuento;
		this.fechaFinDescuento = fechaFinDescuento;
	}
	@ElementCollection
    @CollectionTable(name = "variant_images", joinColumns = @JoinColumn(name = "variant_id"))
    @Column(name = "image_url", columnDefinition = "VARCHAR(MAX)")
    private List<String> imageUrls = new ArrayList<>();

    private LocalDateTime fechaInicioDescuento;
    private LocalDateTime fechaFinDescuento;
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
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public Talla getTalla() {
		return talla;
	}
	public void setTalla(Talla talla) {
		this.talla = talla;
	}
	public int getDiscountPercent() {
        if (activo && stock > 0 && stock <= 10) {
            int original = (this.discountedPrice > 0) ? this.discountedPrice : this.price;
            int current = (int) Math.round(this.price * 0.95);
            if (original > 0) {
                return (int) Math.round(((double)(original - current) / original) * 100);
            }
            return 5;
        }
		return discountPercent;
	}
	public void setDiscountPercent(int discountPercent) {
		this.discountPercent = discountPercent;
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
	public int getPrice() {
        if (activo && stock > 0 && stock <= 10) {
            return (int) Math.round(this.price * 0.95);
        }
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public int getDiscountedPrice() {
        if (activo && stock > 0 && stock <= 10) {
            if (this.discountedPrice > 0) {
                return this.discountedPrice;
            } else {
                return this.price;
            }
        }
		return discountedPrice;
	}
	public void setDiscountedPrice(int discountedPrice) {
		this.discountedPrice = discountedPrice;
	}
	public List<String> getImageUrls() {
		return imageUrls;
	}
	public void setImageUrls(List<String> imageUrls) {
		this.imageUrls = imageUrls;
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