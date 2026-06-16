package com.proyecto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UpdateVariantRequest {
    private String color;
    private String size;
    private int stock;
    private int price;
    private int discountedPrice;
    private List<String> imageUrls;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicioDescuento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFinDescuento;

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(int discountedPrice) { this.discountedPrice = discountedPrice; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
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