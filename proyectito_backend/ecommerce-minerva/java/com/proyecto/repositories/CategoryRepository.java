package com.proyecto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto.model.Categoria;

public interface CategoryRepository extends JpaRepository<Categoria,Long>{

	public Categoria findByName(String name);

    @Query("SELECT c FROM Categoria c WHERE c.name = :name AND c.padrecategoria.name = :parentCategoryName")
    public Categoria findByNameAndParant(@Param("name") String name,
                                         @Param("parentCategoryName") String parentCategoryName);
}
