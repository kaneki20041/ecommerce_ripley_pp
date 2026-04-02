package com.proyecto.request;

public class UpdateProductBasicRequest {
    private String title;
    private String description;
    private String marca;
    private String material;
    private String genero;
    private String categoria;

    // Si tienes checkboxes para estos, los dejamos. Si no, los puedes quitar.
    private boolean isNuevo;
    private boolean isDestacado;

    // --- GETTERS Y SETTERS ---
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
}