package com.proyecto.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.model.Usuario;

public interface UserRepository extends JpaRepository<Usuario,Long>{

	public Usuario findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<Usuario> findByFirstNameAndLastName(String firstName, String Lastname);

    @Override
	List<Usuario> findAll();
}
