package com.proyecto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoriaRequest {

    @NotNull
    @Size(max = 50, message = "El nombre de la categoría no puede exceder los 50 caracteres")
    private String name;

    private Long padreCategoriaId;

    private int level = 1;

    private boolean activo = true;

    public CategoriaRequest() {
    }

    public CategoriaRequest(String name, Long padreCategoriaId, int level, boolean activo) {
        this.name = name;
        this.padreCategoriaId = padreCategoriaId;
        this.level = level;
        this.activo = activo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPadreCategoriaId() {
        return padreCategoriaId;
    }

    public void setPadreCategoriaId(Long padreCategoriaId) {
        this.padreCategoriaId = padreCategoriaId;
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
