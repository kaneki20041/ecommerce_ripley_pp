package com.proyecto.Controller;

import com.proyecto.response.ApiResponse;
import com.proyecto.response.ReportStatDto;
import com.proyecto.service.AdminReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    @Autowired
    private AdminReportService adminReportService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportStatDto>>> getReports(
            @RequestParam(defaultValue = "sales") String type,
            @RequestParam String from,
            @RequestParam String to) {
        
        List<ReportStatDto> stats = adminReportService.getReportStats(type, from, to);
        
        // Use ApiResponse(message, result, data) as requested by the user for endpoints with data
        return ResponseEntity.ok(new ApiResponse<>("Reporte generado exitosamente", true, stats));
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, Object>>>> getReportDetails(
            @RequestParam(defaultValue = "sales") String type,
            @RequestParam String from,
            @RequestParam String to) {
        
        java.util.List<java.util.Map<String, Object>> details = adminReportService.getReportDetails(type, from, to);
        return ResponseEntity.ok(new ApiResponse<>("Detalles generados exitosamente", true, details));
    }
}
