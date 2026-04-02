package com.proyecto.response;

import java.time.LocalDateTime;

import com.proyecto.model.Usuario;

public class AdminUsuarioResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Boolean active;
    private LocalDateTime createdAt;

    // Constructor vacío
    public AdminUsuarioResponse() {}

    // Constructor que recibe la entidad (Súper útil para mapear rápido)
    public AdminUsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.firstName = usuario.getFirstName();
        this.lastName = usuario.getLastName();
        this.email = usuario.getEmail();
        this.role = usuario.getRole().name();
        this.active = usuario.getActive();
        this.createdAt = usuario.getCreatedAt();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}