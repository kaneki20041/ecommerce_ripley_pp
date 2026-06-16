package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.response.AdminDashboardResponse;
import com.proyecto.response.ApiResponse;
import com.proyecto.service.AdminDashboardService;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    /**
     * GET /api/admin/dashboard/stats?period=30d
     * Devuelve todas las métricas del dashboard analítico para el administrador.
     * @param period  "7d" | "30d" | "90d" | "365d"  (default: "30d")
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboardStats(
            @RequestParam(defaultValue = "30d") String period) {
        AdminDashboardResponse data = adminDashboardService.getDashboardStats(period);
        return ResponseEntity.ok(
                new ApiResponse<>("Dashboard analítico cargado con éxito", true, data));
    }
}
