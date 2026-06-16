package com.proyecto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.model.SmartCartConfig;

@Repository
public interface SmartCartConfigRepository extends JpaRepository<SmartCartConfig, Long> {
}
