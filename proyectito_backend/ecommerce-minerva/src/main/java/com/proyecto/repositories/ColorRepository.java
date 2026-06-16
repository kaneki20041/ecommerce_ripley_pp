package com.proyecto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.model.Color;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    Color findByNombre(String nombre);
}
