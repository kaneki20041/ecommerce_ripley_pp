package com.proyecto.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.model.Talla;
import com.proyecto.repositories.TallaRepository;
import com.proyecto.response.ApiResponse;

@RestController
@RequestMapping("/api/tallas")
public class TallaController {

    private final TallaRepository tallaRepository;

    public TallaController(TallaRepository tallaRepository) {
        this.tallaRepository = tallaRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Talla>>> getAllTallas() {
        List<Talla> tallas = tallaRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>("Tallas obtenidas exitosamente", true, tallas));
    }
}
