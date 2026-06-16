package com.proyecto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyecto.model.Favorite;
import com.proyecto.model.Usuario;
import com.proyecto.model.Product;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserOrderByAddedAtDesc(Usuario user);
    Optional<Favorite> findByUserAndProduct(Usuario user, Product product);
    boolean existsByUserAndProduct(Usuario user, Product product);
    void deleteByUserAndProduct(Usuario user, Product product);
}
