package com.proyecto.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.model.Categoria;
import com.proyecto.repositories.CategoryRepository;
import com.proyecto.request.CategoriaRequest;
import com.proyecto.response.CategoriaFlatResponse;
import com.proyecto.response.CategoriaResponse;

@Service
public class CategoriaServiceImplementation implements CategoriaService {

    private final CategoryRepository categoryRepository;

    public CategoriaServiceImplementation(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoryTree(boolean includeInactive) {
        // 1. Obtener todas las categorías de una sola vez para evitar consultas N+1
        List<Categoria> allCategories = categoryRepository.findAll();

        // 2. Mapear a CategoriaResponse y crear un mapa para acceso rápido
        Map<Long, CategoriaResponse> responseMap = new HashMap<>();
        List<CategoriaResponse> allResponses = new ArrayList<>();

        for (Categoria cat : allCategories) {
            // Si includeInactive es false y la categoría está inactiva, la omitimos
            if (!includeInactive && !cat.isActivo()) {
                continue;
            }

            Long padreId = cat.getPadrecategoria() != null ? cat.getPadrecategoria().getId() : null;
            String padreName = cat.getPadrecategoria() != null ? cat.getPadrecategoria().getName() : null;

            CategoriaResponse resp = new CategoriaResponse(
                cat.getId(),
                cat.getName(),
                padreId,
                padreName,
                cat.getLevel(),
                cat.isActivo()
            );

            responseMap.put(cat.getId(), resp);
            allResponses.add(resp);
        }

        // 3. Reconstruir la jerarquía en memoria
        List<CategoriaResponse> rootCategories = new ArrayList<>();

        for (CategoriaResponse resp : allResponses) {
            if (resp.getPadreId() == null) {
                // Es de Nivel 1 (Root)
                rootCategories.add(resp);
            } else {
                // Buscar su padre en el mapa y agregarlo a su lista de subcategorías
                CategoriaResponse padreResp = responseMap.get(resp.getPadreId());
                if (padreResp != null) {
                    padreResp.addSubcategory(resp);
                } else {
                    // Si el padre fue omitido por estar inactivo, tratamos a la hija como huérfana temporal
                    // o la omitimos si su padre no está en el mapa. En este caso, si el padre está inactivo,
                    // no deberíamos listar sus subcategorías. Si includeInactive es true, siempre estará.
                    if (includeInactive) {
                        rootCategories.add(resp);
                    }
                }
            }
        }

        return rootCategories;
    }

    @Override
    @Transactional
    public CategoriaResponse createCategory(CategoriaRequest req) {
        Categoria parent = null;
        int level = 1;

        if (req.getPadreCategoriaId() != null) {
            parent = categoryRepository.findById(req.getPadreCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría padre no encontrada con el ID: " + req.getPadreCategoriaId()));
            level = parent.getLevel() + 1;

            if (level > 3) {
                throw new IllegalArgumentException("No se permite crear categorías de un nivel mayor a 3");
            }
        }

        Categoria cat = new Categoria();
        cat.setName(req.getName());
        cat.setPadrecategoria(parent);
        cat.setLevel(level);
        cat.setActivo(req.isActivo());

        Categoria savedCat = categoryRepository.save(cat);
        return mapToResponse(savedCat);
    }

    @Override
    @Transactional
    public CategoriaResponse updateCategory(Long id, CategoriaRequest req) {
        Categoria cat = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con el ID: " + id));

        cat.setName(req.getName());
        cat.setActivo(req.isActivo());

        // Manejo de cambio de padre
        if (req.getPadreCategoriaId() != null) {
            if (req.getPadreCategoriaId().equals(id)) {
                throw new IllegalArgumentException("Una categoría no puede ser su propio padre");
            }

            Categoria parent = categoryRepository.findById(req.getPadreCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría padre no encontrada con el ID: " + req.getPadreCategoriaId()));
            
            cat.setPadrecategoria(parent);
            cat.setLevel(parent.getLevel() + 1);

            if (cat.getLevel() > 3) {
                throw new IllegalArgumentException("No se permite crear categorías de un nivel mayor a 3");
            }
        } else {
            cat.setPadrecategoria(null);
            cat.setLevel(1);
        }

        Categoria savedCat = categoryRepository.save(cat);
        return mapToResponse(savedCat);
    }

    @Override
    @Transactional
    public CategoriaResponse toggleCategoryStatus(Long id) {
        Categoria cat = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con el ID: " + id));

        cat.setActivo(!cat.isActivo());
        Categoria savedCat = categoryRepository.save(cat);
        return mapToResponse(savedCat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaFlatResponse> getAllFlat(boolean includeInactive) {
        List<Categoria> allCategories = categoryRepository.findAll();

        return allCategories.stream()
            .filter(cat -> includeInactive || cat.isActivo())
            .map(cat -> {
                Long padreId = cat.getPadrecategoria() != null ? cat.getPadrecategoria().getId() : null;
                String padreName = cat.getPadrecategoria() != null ? cat.getPadrecategoria().getName() : null;
                return new CategoriaFlatResponse(
                    cat.getId(),
                    cat.getName(),
                    padreId,
                    padreName,
                    cat.getLevel(),
                    cat.isActivo()
                );
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse getCategoryById(Long id) {
        Categoria cat = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con el ID: " + id));

        return mapToResponse(cat);
    }

    private CategoriaResponse mapToResponse(Categoria cat) {
        Long padreId = cat.getPadrecategoria() != null ? cat.getPadrecategoria().getId() : null;
        String padreName = cat.getPadrecategoria() != null ? cat.getPadrecategoria().getName() : null;

        return new CategoriaResponse(
            cat.getId(),
            cat.getName(),
            padreId,
            padreName,
            cat.getLevel(),
            cat.isActivo()
        );
    }
}
