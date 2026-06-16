package com.proyecto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.model.CrmConfig;

@Repository
public interface CrmConfigRepository extends JpaRepository<CrmConfig, Long> {
}
