package com.proyecto.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.SmartCartConfig;
import com.proyecto.request.SmartCartConfigRequest;
import com.proyecto.response.ApiResponse;
import com.proyecto.service.SmartCartConfigService;

@RestController
@RequestMapping("/api")
public class SmartCartConfigController {

    private final SmartCartConfigService smartCartConfigService;

    public SmartCartConfigController(SmartCartConfigService smartCartConfigService) {
        this.smartCartConfigService = smartCartConfigService;
    }

    @GetMapping({"/admin/smart-cart/config", "/public/smart-cart/config"})
    public ResponseEntity<ApiResponse<SmartCartConfig>> getConfig() {
        SmartCartConfig config = smartCartConfigService.getConfig();
        return new ResponseEntity<>(new ApiResponse<>("Configuración recuperada", true, config), HttpStatus.OK);
    }

    @PutMapping("/admin/smart-cart/config")
    public ResponseEntity<ApiResponse<SmartCartConfig>> updateConfig(@RequestBody SmartCartConfigRequest req) throws ProductException {
        SmartCartConfig updated = smartCartConfigService.updateConfig(req);
        return new ResponseEntity<>(new ApiResponse<>("Configuración actualizada exitosamente", true, updated), HttpStatus.OK);
    }
}
