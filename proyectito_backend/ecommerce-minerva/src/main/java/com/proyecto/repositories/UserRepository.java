package com.proyecto.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proyecto.model.Usuario;

public interface UserRepository extends JpaRepository<Usuario,Long>{

	public Usuario findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<Usuario> findByFirstNameAndLastName(String firstName, String Lastname);

    @Override
	List<Usuario> findAll();

    @Query("SELECT u FROM Usuario u WHERE u.role = 'USER' AND u.active = true " +
           "AND NOT EXISTS (SELECT o FROM Order o WHERE o.user = u AND o.orderDate > :orderThresholdDate) " +
           "AND (u.lastReactivationEmailSent IS NULL OR u.lastReactivationEmailSent < :emailThresholdDate)")
    List<Usuario> findChurnCandidates(@org.springframework.data.repository.query.Param("orderThresholdDate") java.time.LocalDateTime orderThresholdDate, 
                                      @org.springframework.data.repository.query.Param("emailThresholdDate") java.time.LocalDateTime emailThresholdDate);

    @Query("SELECT u FROM Usuario u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate ORDER BY u.createdAt DESC")
    List<Usuario> findByCreatedAtBetween(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, 
                                         @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);
}
