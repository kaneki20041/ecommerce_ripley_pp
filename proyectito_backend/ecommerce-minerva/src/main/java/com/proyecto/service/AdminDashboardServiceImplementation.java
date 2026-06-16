package com.proyecto.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.model.Order;
import com.proyecto.model.OrderItem;
import com.proyecto.repositories.OrderRepository;
import com.proyecto.repositories.ProductRepository;
import com.proyecto.repositories.UserRepository;
import com.proyecto.response.AdminDashboardResponse;
import com.proyecto.response.AdminDashboardResponse.*;

@Service
public class AdminDashboardServiceImplementation implements AdminDashboardService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private com.proyecto.repositories.RecommendationMetricRepository recommendationMetricRepository;

    // ─── Helpers de período ───────────────────────────────────────────────────

    private LocalDateTime startOf(String period) {
        return switch (period) {
            case "7d"   -> LocalDateTime.now().minusDays(7);
            case "90d"  -> LocalDateTime.now().minusDays(90);
            case "365d" -> LocalDateTime.now().minusDays(365);
            default     -> LocalDateTime.now().minusDays(30); // "30d"
        };
    }

    private int daysOf(String period) {
        return switch (period) {
            case "7d"   -> 7;
            case "90d"  -> 90;
            case "365d" -> 365;
            default     -> 30;
        };
    }

    private double getRevenue(Order o) {
        if (o.getTotalDiscountedPrice() != null && o.getTotalDiscountedPrice() > 0)
            return o.getTotalDiscountedPrice();
        return o.getTotalPrice();
    }

    private boolean isCompleted(Order o) {
        String s = o.getOrderStatus();
        return s != null && (
            s.equalsIgnoreCase("DELIVERED") || 
            s.equalsIgnoreCase("ENTREGADO") || 
            s.equalsIgnoreCase("COMPLETADO") || 
            s.equalsIgnoreCase("PICKED_UP")
        );
    }

    private boolean isPending(Order o) {
        String s = o.getOrderStatus();
        return s != null && (
            s.equalsIgnoreCase("PENDING") || 
            s.equalsIgnoreCase("PLACED") || 
            s.equalsIgnoreCase("CONFIRMED") || 
            s.equalsIgnoreCase("PROCESSING") || 
            s.equalsIgnoreCase("PROCESO") ||
            s.equalsIgnoreCase("READY_FOR_PICKUP") ||
            s.equalsIgnoreCase("SHIPPED") ||
            s.equalsIgnoreCase("ENVIADO")
        );
    }

    private LocalDateTime getOrderDate(Order o) {
        return o.getCreateAt() != null ? o.getCreateAt() : o.getOrderDate();
    }

    // ─── Implementación principal ─────────────────────────────────────────────

    @Override
    public AdminDashboardResponse getDashboardStats(String period) {
        LocalDateTime start     = startOf(period);
        LocalDateTime prevStart = start.minusDays(daysOf(period));

        List<Order> allOrders  = orderRepository.findAll();
        List<Order> currOrders = allOrders.stream()
                .filter(o -> getOrderDate(o) != null && getOrderDate(o).isAfter(start))
                .collect(Collectors.toList());
        List<Order> prevOrders = allOrders.stream()
                .filter(o -> { LocalDateTime d = getOrderDate(o);
                    return d != null && d.isAfter(prevStart) && d.isBefore(start); })
                .collect(Collectors.toList());

        // ── KPIs ──────────────────────────────────────────────────────────────
        double currRevenue = currOrders.stream().filter(this::isCompleted).mapToDouble(this::getRevenue).sum();
        double prevRevenue = prevOrders.stream().filter(this::isCompleted).mapToDouble(this::getRevenue).sum();

        int currOrderCount = currOrders.size();
        int prevOrderCount = prevOrders.size();

        long totalUsers    = userRepository.count();
        long totalProducts = productRepository.count();

        double avgOrderValue  = currOrderCount > 0 ? currRevenue / currOrderCount : 0;
        double conversionRate = totalUsers > 0 ? (currOrderCount * 100.0) / totalUsers : 0;

        double revenueGrowth = prevRevenue > 0 ? ((currRevenue - prevRevenue) / prevRevenue) * 100 : 0;
        double ordersGrowth  = prevOrderCount > 0 ? ((currOrderCount - prevOrderCount) / (double) prevOrderCount) * 100 : 0;

        // ── Ventas diarias ────────────────────────────────────────────────────
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, double[]> dailyMap = new TreeMap<>();

        int days = daysOf(period);
        for (int i = days - 1; i >= 0; i--) {
            String key = LocalDateTime.now().minusDays(i).format(fmt);
            dailyMap.put(key, new double[]{0, 0}); // [revenue, orders]
        }

        currOrders.stream().filter(this::isCompleted).forEach(o -> {
            String key = getOrderDate(o).format(fmt);
            if (dailyMap.containsKey(key)) {
                dailyMap.get(key)[0] += getRevenue(o);
                dailyMap.get(key)[1]++;
            }
        });

        List<DailySaleDto> dailySales = dailyMap.entrySet().stream()
                .map(e -> new DailySaleDto(e.getKey(), e.getValue()[0], (int) e.getValue()[1]))
                .collect(Collectors.toList());

        // ── Por estado ────────────────────────────────────────────────────────
        Map<String, Long> statusMap = currOrders.stream()
                .collect(Collectors.groupingBy(o -> normalizeStatus(o.getOrderStatus()), Collectors.counting()));

        int totalCount = currOrders.size();
        List<StatusCountDto> byStatus = statusMap.entrySet().stream()
                .map(e -> {
                    StatusCountDto d = new StatusCountDto(e.getKey(), e.getValue().intValue());
                    d.setPercentage(totalCount > 0 ? (e.getValue() * 100.0) / totalCount : 0);
                    return d;
                })
                .sorted(Comparator.comparingInt(StatusCountDto::getCount).reversed())
                .collect(Collectors.toList());

        // ── Por categoría ─────────────────────────────────────────────────────
        Map<String, double[]> catMap = new HashMap<>();
        currOrders.stream().filter(this::isCompleted).forEach(o -> {
            for (OrderItem item : o.getOrderItems()) {
                String cat = (item.getProduct() != null && item.getProduct().getCategoria() != null)
                        ? item.getProduct().getCategoria().getName() : "General";
                catMap.computeIfAbsent(cat, k -> new double[]{0, 0});
                double price = item.getDiscountedPrice() > 0 ? item.getDiscountedPrice() : item.getPrice();
                catMap.get(cat)[0] += price * item.getQuantity();
                catMap.get(cat)[1]++;
            }
        });

        double totalCatRevenue = catMap.values().stream().mapToDouble(v -> v[0]).sum();
        List<CategoryRevenueDto> topCategories = catMap.entrySet().stream()
                .map(e -> {
                    CategoryRevenueDto d = new CategoryRevenueDto(e.getKey(), e.getValue()[0], (int) e.getValue()[1]);
                    d.setPercentage(totalCatRevenue > 0 ? (e.getValue()[0] * 100.0) / totalCatRevenue : 0);
                    return d;
                })
                .sorted(Comparator.comparingDouble(CategoryRevenueDto::getRevenue).reversed())
                .limit(6)
                .collect(Collectors.toList());

        // ── Últimos pedidos ───────────────────────────────────────────────────
        List<RecentOrderDto> recentOrders = allOrders.stream()
                .sorted(Comparator.comparing(o -> getOrderDate(o) != null ? getOrderDate(o) : LocalDateTime.MIN, Comparator.reverseOrder()))
                .limit(10)
                .map(o -> {
                    String customer = o.getShippingAddress() != null
                            ? (o.getShippingAddress().getFirstName() + " " + o.getShippingAddress().getLastName()).trim()
                            : (o.getUser() != null ? o.getUser().getFirstName() : "Cliente");
                    String dateStr = getOrderDate(o) != null ? getOrderDate(o).format(fmt) : "N/A";
                    String pm = (o.getPaymentDetails() != null && o.getPaymentDetails().getPaymentMethod() != null)
                            ? o.getPaymentDetails().getPaymentMethod() : "Mercado Pago";
                    return new RecentOrderDto("#" + o.getId(), customer, getRevenue(o),
                            normalizeStatus(o.getOrderStatus()), dateStr, pm);
                })
                .collect(Collectors.toList());

        // ── Armar respuesta ───────────────────────────────────────────────────
        AdminDashboardResponse resp = new AdminDashboardResponse();
        resp.setTotalRevenue(currRevenue);
        resp.setTotalOrders(currOrderCount);
        resp.setTotalUsers((int) totalUsers);
        resp.setTotalProducts((int) totalProducts);
        resp.setAvgOrderValue(avgOrderValue);
        resp.setConversionRate(conversionRate);
        resp.setRevenueGrowth(revenueGrowth);
        resp.setOrdersGrowth(ordersGrowth);
        resp.setUsersGrowth(0);
        resp.setDailySales(dailySales);
        resp.setTopCategories(topCategories);
        resp.setOrdersByStatus(byStatus);
        resp.setRecentOrders(recentOrders);

        // Calcular CTR de recomendaciones
        Long totalImpressions = recommendationMetricRepository.sumTotalImpressions();
        Long totalClicks = recommendationMetricRepository.sumTotalClicks();
        double ctr = 0.0;
        if (totalImpressions != null && totalImpressions > 0) {
            long clicksVal = totalClicks != null ? totalClicks : 0L;
            ctr = (clicksVal * 100.0) / totalImpressions;
        }
        resp.setRecommendationCtr(ctr);

        return resp;
    }

    private String normalizeStatus(String s) {
        if (s == null) return "Desconocido";
        return switch (s.toUpperCase()) {
            case "PENDING", "PLACED", "PENDIENTE"          -> "Pendiente";
            case "CONFIRMED", "PROCESO"                    -> "Confirmado";
            case "PROCESSING"                              -> "En proceso";
            case "SHIPPED", "ENVIADO"                      -> "Enviado";
            case "READY_FOR_PICKUP"                        -> "Listo para retiro";
            case "DELIVERED", "ENTREGADO", "COMPLETADO", "PICKED_UP" -> "Entregado";
            case "CANCELLED", "CANCELED"                   -> "Cancelado";
            default -> s;
        };
    }
}
