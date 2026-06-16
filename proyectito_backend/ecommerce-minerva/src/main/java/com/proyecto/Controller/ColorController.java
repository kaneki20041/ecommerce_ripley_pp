package com.proyecto.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.model.Color;
import com.proyecto.repositories.ColorRepository;
import com.proyecto.response.ApiResponse;

@RestController
@RequestMapping("/api/colors")
public class ColorController {

    private final ColorRepository colorRepository;

    public ColorController(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Color>>> getAllColors() {
        List<Color> colors = colorRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>("Colores obtenidos exitosamente", true, colors));
    }
}
