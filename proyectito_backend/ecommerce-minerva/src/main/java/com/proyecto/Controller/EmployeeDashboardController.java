package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.response.ApiResponse;
import com.proyecto.response.EmployeeDashboardResponse;
import com.proyecto.service.EmployeeDashboardService;

@RestController
@RequestMapping("/api/employee/dashboard")
public class EmployeeDashboardController {

    @Autowired
    private EmployeeDashboardService employeeDashboardService;

    /**
     * GET /api/employee/dashboard/stats?period=30d
     * Devuelve métricas operacionales del dashboard para el empleado.
     * @param period  "7d" | "30d" | "90d"  (default: "30d")
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<EmployeeDashboardResponse>> getDashboardStats(
            @RequestParam(defaultValue = "30d") String period) {
        EmployeeDashboardResponse data = employeeDashboardService.getDashboardStats(period);
        return ResponseEntity.ok(
                new ApiResponse<>("Dashboard operacional cargado con éxito", true, data));
    }
}
