package com.proyecto.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tallas")
@Data
public class Talla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String valor; 

    @Column(nullable = false, length = 50)
    private String tipo;
}