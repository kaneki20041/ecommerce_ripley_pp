package com.proyecto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CreateProductRequest {
    private String title;
    private String description;
    private int price;
    private int descuentoprice;
    private int stockTotal; // Lo usaremos para repartir el stock entre las variantes

    private String marca;
    private String material;
    private String genero;
    private String categoria;

    private List<ColorData> coloresData;

    private boolean isNuevo;
    private boolean isDestacado;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicioDescuento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFinDescuento;
    public static class ColorData {
        private String nombreColor;
        private List<String> imagenesColor;
        private List<String> sizesColor;

        private int price;
        private int descuentoprice;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime fechaInicioDescuento;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime fechaFinDescuento;

        // Getters y Setters
        public String getNombreColor() { return nombreColor; }
        public void setNombreColor(String nombreColor) { this.nombreColor = nombreColor; }

        public List<String> getImagenesColor() { return imagenesColor; }
        public void setImagenesColor(List<String> imagenesColor) { this.imagenesColor = imagenesColor; }

        public List<String> getSizesColor() { return sizesColor; }
        public void setSizesColor(List<String> sizesColor) { this.sizesColor = sizesColor; }

        public int getPrice() { return price; }
        public void setPrice(int price) { this.price = price; }

        public int getDescuentoprice() { return descuentoprice; }
        public void setDescuentoprice(int descuentoprice) { this.descuentoprice = descuentoprice; }
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
	public int getStockTotal() {
		return stockTotal;
	}
	public void setStockTotal(int stockTotal) {
		this.stockTotal = stockTotal;
	}
	public String getMarca() {
		return marca;
	}
	public void setMarca(String marca) {
		this.marca = marca;
	}
	public String getMaterial() {
		return material;
	}
	public void setMaterial(String material) {
		this.material = material;
	}
	public String getGenero() {
		return genero;
	}
	public void setGenero(String genero) {
		this.genero = genero;
	}
	public String getCategoria() {
		return categoria;
	}
	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public boolean isNuevo() {
		return isNuevo;
	}

	public List<ColorData> getColoresData() {
		return coloresData;
	}
	public void setColoresData(List<ColorData> coloresData) {
		this.coloresData = coloresData;
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