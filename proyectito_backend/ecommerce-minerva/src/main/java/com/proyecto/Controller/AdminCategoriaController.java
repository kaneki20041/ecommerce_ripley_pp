package com.proyecto.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.request.CategoriaRequest;
import com.proyecto.response.ApiResponse;
import com.proyecto.response.CategoriaFlatResponse;
import com.proyecto.response.CategoriaResponse;
import com.proyecto.service.CategoriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoriaController {

    private final CategoriaService categoriaService;

    public AdminCategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> getAdminCategoryTree() {
        List<CategoriaResponse> tree = categoriaService.getCategoryTree(true);
        return ResponseEntity.ok(new ApiResponse<>("Árbol administrativo de categorías recuperado con éxito", true, tree));
    }

    @GetMapping("/flat")
    public ResponseEntity<ApiResponse<List<CategoriaFlatResponse>>> getAdminFlatCategories() {
        List<CategoriaFlatResponse> list = categoriaService.getAllFlat(true);
        return ResponseEntity.ok(new ApiResponse<>("Lista plana de categorías recuperada con éxito", true, list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> getCategoryById(@PathVariable Long id) {
        try {
            CategoriaResponse resp = categoriaService.getCategoryById(id);
            return ResponseEntity.ok(new ApiResponse<>("Categoría recuperada con éxito", true, resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(e.getMessage(), false, null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaResponse>> createCategory(@Valid @RequestBody CategoriaRequest req) {
        try {
            CategoriaResponse resp = categoriaService.createCategory(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Categoría creada con éxito", true, resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(e.getMessage(), false, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoriaRequest req) {
        try {
            CategoriaResponse resp = categoriaService.updateCategory(id, req);
            return ResponseEntity.ok(new ApiResponse<>("Categoría actualizada con éxito", true, resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(e.getMessage(), false, null));
        }
    }

    @PutMapping("/toggle/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> toggleCategoryStatus(@PathVariable Long id) {
        try {
            CategoriaResponse resp = categoriaService.toggleCategoryStatus(id);
            String estado = resp.isActivo() ? "activada" : "desactivada (soft-delete)";
            return ResponseEntity.ok(new ApiResponse<>("Categoría " + estado + " con éxito", true, resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(e.getMessage(), false, null));
        }
    }
}
