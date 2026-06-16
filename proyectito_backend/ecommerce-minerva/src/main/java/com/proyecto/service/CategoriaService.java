package com.proyecto.service;

import java.util.List;
import com.proyecto.request.CategoriaRequest;
import com.proyecto.response.CategoriaFlatResponse;
import com.proyecto.response.CategoriaResponse;

public interface CategoriaService {

    List<CategoriaResponse> getCategoryTree(boolean includeInactive);

    CategoriaResponse createCategory(CategoriaRequest req);

    CategoriaResponse updateCategory(Long id, CategoriaRequest req);

    CategoriaResponse toggleCategoryStatus(Long id);

    List<CategoriaFlatResponse> getAllFlat(boolean includeInactive);

    CategoriaResponse getCategoryById(Long id);
}
