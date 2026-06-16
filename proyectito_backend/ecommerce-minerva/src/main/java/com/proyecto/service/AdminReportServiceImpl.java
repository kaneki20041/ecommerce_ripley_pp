package com.proyecto.service;

import com.proyecto.repositories.OrderRepository;
import com.proyecto.response.ReportStatDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminReportServiceImpl implements AdminReportService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<ReportStatDto> getReportStats(String type, String fromDateStr, String toDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDateTime startDate;
        LocalDateTime endDate;
        
        try {
            LocalDate fromDate = LocalDate.parse(fromDateStr, formatter);
            LocalDate toDate = LocalDate.parse(toDateStr, formatter);
            startDate = fromDate.atStartOfDay();
            endDate = toDate.atTime(LocalTime.MAX);
        } catch (Exception e) {
            startDate = LocalDateTime.now().minusDays(30);
            endDate = LocalDateTime.now();
        }

        List<ReportStatDto> stats = new ArrayList<>();

        if ("sales".equalsIgnoreCase(type) || "products".equalsIgnoreCase(type) || "users".equalsIgnoreCase(type)) {
            // General Sales Report
            Double totalRevenue = orderRepository.sumTotalRevenueByDateRange(startDate, endDate);
            if (totalRevenue == null) totalRevenue = 0.0;
            
            Long totalOrders = orderRepository.countOrdersByDateRange(startDate, endDate);
            if (totalOrders == null) totalOrders = 0L;
            
            Long totalProducts = orderRepository.sumProductsSoldByDateRange(startDate, endDate);
            if (totalProducts == null) totalProducts = 0L;

            stats.add(new ReportStatDto("Ventas Totales", String.format("S/ %,.2f", totalRevenue), "dollar", "#7CB8D1"));
            stats.add(new ReportStatDto("Pedidos", String.format("%,d", totalOrders), "shopping-bag", "#4CAF50"));
            stats.add(new ReportStatDto("Productos Vendidos", String.format("%,d", totalProducts), "box", "#FFD93D"));
        }

        return stats;
    }

    @Autowired
    private com.proyecto.repositories.UserRepository userRepository;

    @Autowired
    private com.proyecto.repositories.ProductRepository productRepository;

    @Override
    public List<Map<String, Object>> getReportDetails(String type, String fromDateStr, String toDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDateTime startDate;
        LocalDateTime endDate;
        
        try {
            LocalDate fromDate = LocalDate.parse(fromDateStr, formatter);
            LocalDate toDate = LocalDate.parse(toDateStr, formatter);
            startDate = fromDate.atStartOfDay();
            endDate = toDate.atTime(LocalTime.MAX);
        } catch (Exception e) {
            startDate = LocalDateTime.now().minusDays(30);
            endDate = LocalDateTime.now();
        }

        List<Map<String, Object>> details = new ArrayList<>();

        if ("sales".equalsIgnoreCase(type)) {
            List<com.proyecto.model.Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
            for (com.proyecto.model.Order o : orders) {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("ID", o.getId());
                map.put("Fecha", o.getOrderDate() != null ? o.getOrderDate().toLocalDate().toString() : "");
                map.put("Cliente", o.getUser() != null ? o.getUser().getEmail() : "");
                map.put("Estado", o.getOrderStatus());
                map.put("Total", "S/ " + String.format("%,.2f", o.getTotalDiscountedPrice() > 0 ? o.getTotalDiscountedPrice() : o.getTotalPrice()));
                details.add(map);
            }
        } else if ("users".equalsIgnoreCase(type)) {
            List<com.proyecto.model.Usuario> users = userRepository.findByCreatedAtBetween(startDate, endDate);
            for (com.proyecto.model.Usuario u : users) {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("ID", u.getId());
                map.put("Nombre", u.getFirstName());
                map.put("Apellido", u.getLastName());
                map.put("Email", u.getEmail());
                map.put("Fecha de Registro", u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : "");
                details.add(map);
            }
        } else if ("products".equalsIgnoreCase(type)) {
            List<com.proyecto.model.Product> products = productRepository.findByCreatedAtBetween(startDate, endDate);
            for (com.proyecto.model.Product p : products) {
                Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("ID", p.getId());
                map.put("Título", p.getTitle());
                map.put("Precio", "S/ " + p.getPrice());
                map.put("Categoría", p.getCategoria() != null ? p.getCategoria().getName() : "");
                map.put("Fecha de Creación", p.getCreatedAt() != null ? p.getCreatedAt().toLocalDate().toString() : "");
                details.add(map);
            }
        }

        return details;
    }
}
