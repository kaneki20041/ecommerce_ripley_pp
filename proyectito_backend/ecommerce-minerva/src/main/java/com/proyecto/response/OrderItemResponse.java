package com.proyecto.response;

import com.proyecto.model.Product;
import java.time.LocalDateTime;

public class OrderItemResponse {
    private Long id;
    private Product product;
    private String size;
    private String color;
    private int quantity;
    private Integer price;
    private Integer discountedPrice;
    private LocalDateTime deliveryDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(Integer discountedPrice) { this.discountedPrice = discountedPrice; }
    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
}
