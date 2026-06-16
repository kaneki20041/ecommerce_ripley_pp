package com.proyecto.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.response.ApiResponse;
import com.proyecto.response.CategoriaResponse;
import com.proyecto.service.CategoriaService;

@RestController
@RequestMapping("/api/public/categories")
public class PublicCategoriaController {

    private final CategoriaService categoriaService;

    public PublicCategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> getPublicCategoryTree() {
        List<CategoriaResponse> tree = categoriaService.getCategoryTree(false);
        return ResponseEntity.ok(new ApiResponse<>("Árbol de categorías recuperado con éxito", true, tree));
    }
}
