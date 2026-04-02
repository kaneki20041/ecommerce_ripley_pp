package com.proyecto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.proyecto.model.Product;
import com.proyecto.model.ProductVariant;

public class SingleVariantResponse {

    private Long productId;
    private String title;
    private String description;
    private String marca;
    private String material;
    private String genero;
    private String categoria;
    private boolean isNuevo;
    private boolean isDestacado;

    // --- DATOS DE LA VARIANTE ESPECÍFICA (Precios, Stock y Fotos) ---
    private Long variantId;
    private String color;
    private String size;
    private int stock;
    private int price;
    private int descuentoprice;
    private List<String> imageUrls;
    private boolean activo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicioDescuento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFinDescuento;

    // Constructor que fusiona ambos objetos
    public SingleVariantResponse(Product product, ProductVariant variant) {

        // Mapeo del padre
        this.productId = product.getId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.marca = product.getMarca();
        this.material = product.getMaterial();
        this.genero = product.getGenero();
        this.categoria = product.getCategoria() != null ? product.getCategoria().getName() : "";
        this.isNuevo = product.isNuevo();
        this.isDestacado = product.isDestacado();

        // Mapeo de la hija
        this.variantId = variant.getId();
        this.color = variant.getColor();
        this.size = variant.getSize();
        this.stock = variant.getStock();
        this.price = variant.getPrice();
        this.descuentoprice = variant.getDescuentoprice();
        this.imageUrls = variant.getImageUrls();
        this.activo = variant.isActivo();
        this.fechaInicioDescuento = variant.getFechaInicioDescuento();
        this.fechaFinDescuento = variant.getFechaFinDescuento();
    }

    // --- GETTERS Y SETTERS ---
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public boolean isNuevo() { return isNuevo; }
    public void setNuevo(boolean isNuevo) { this.isNuevo = isNuevo; }

    public boolean isDestacado() { return isDestacado; }
    public void setDestacado(boolean isDestacado) { this.isDestacado = isDestacado; }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getDescuentoprice() { return descuentoprice; }
    public void setDescuentoprice(int descuentoprice) { this.descuentoprice = descuentoprice; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

}