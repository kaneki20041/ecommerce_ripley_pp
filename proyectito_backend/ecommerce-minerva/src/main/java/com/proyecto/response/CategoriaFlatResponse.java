package com.proyecto.response;

public class CategoriaFlatResponse {

    private Long id;
    private String name;
    private Long padreId;
    private String padreName;
    private int level;
    private boolean activo;

    public CategoriaFlatResponse() {
    }

    public CategoriaFlatResponse(Long id, String name, Long padreId, String padreName, int level, boolean activo) {
        this.id = id;
        this.name = name;
        this.padreId = padreId;
        this.padreName = padreName;
        this.level = level;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPadreId() {
        return padreId;
    }

    public void setPadreId(Long padreId) {
        this.padreId = padreId;
    }

    public String getPadreName() {
        return padreName;
    }

    public void setPadreName(String padreName) {
        this.padreName = padreName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
