package com.proyecto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.model.Talla;

@Repository
public interface TallaRepository extends JpaRepository<Talla, Long> {
    Talla findByValor(String valor);
}
