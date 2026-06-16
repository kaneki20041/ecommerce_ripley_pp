package com.proyecto.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "colores")
@Data
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre; // Ej: "Azul Marino", "Blanco"

    @Column(name = "hex_code", nullable = false, length = 7)
    private String hexCode; // Ej: "#000080", "#FFFFFF"
}