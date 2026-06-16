package com.proyecto.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyecto.model.Order;
import com.proyecto.repositories.OrderRepository;
import com.proyecto.repositories.ProductVariantRepository;
import com.proyecto.response.EmployeeDashboardResponse;
import com.proyecto.response.EmployeeDashboardResponse.*;

@Service
public class EmployeeDashboardServiceImplementation implements EmployeeDashboardService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductVariantRepository productVariantRepository;

    private LocalDateTime startOf(String period) {
        return switch (period) {
            case "7d"  -> LocalDateTime.now().minusDays(7);
            case "90d" -> LocalDateTime.now().minusDays(90);
            default    -> LocalDateTime.now().minusDays(30);
        };
    }

    private int daysOf(String period) {
        return switch (period) { case "7d" -> 7; case "90d" -> 90; default -> 30; };
    }

    private LocalDateTime getDate(Order o) {
        return o.getCreateAt() != null ? o.getCreateAt() : o.getOrderDate();
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

    private boolean isCompleted(Order o) {
        String s = o.getOrderStatus();
        return s != null && (
            s.equalsIgnoreCase("DELIVERED") || 
            s.equalsIgnoreCase("ENTREGADO") || 
            s.equalsIgnoreCase("COMPLETADO") || 
            s.equalsIgnoreCase("PICKED_UP")
        );
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

    @Override
    public EmployeeDashboardResponse getDashboardStats(String period) {
        LocalDateTime start = startOf(period);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Order> allOrders   = orderRepository.findAll();
        List<Order> periodOrders = allOrders.stream()
                .filter(o -> getDate(o) != null && getDate(o).isAfter(start))
                .collect(Collectors.toList());

        List<Order> pendingList   = periodOrders.stream().filter(this::isPending).collect(Collectors.toList());
        List<Order> completedList = periodOrders.stream().filter(this::isCompleted).collect(Collectors.toList());

        int totalInPeriod   = periodOrders.size();
        int pendingCount    = pendingList.size();
        int completedCount  = completedList.size();
        long lowStockCount  = productVariantRepository.countLowStockVariants();

        double totalCompletedRevenue = completedList.stream()
                .mapToDouble(o -> o.getTotalDiscountedPrice() != null && o.getTotalDiscountedPrice() > 0
                        ? o.getTotalDiscountedPrice() : o.getTotalPrice())
                .sum();
        double avgRevenue     = completedCount > 0 ? totalCompletedRevenue / completedCount : 0;
        double processingRate = totalInPeriod > 0 ? (completedCount * 100.0) / totalInPeriod : 0;

        // ── Pedidos por día ───────────────────────────────────────────────────
        Map<String, int[]> dailyMap = new TreeMap<>();
        for (int i = daysOf(period) - 1; i >= 0; i--) {
            dailyMap.put(LocalDateTime.now().minusDays(i).format(fmt), new int[]{0, 0}); // [pending, completed]
        }
        periodOrders.forEach(o -> {
            String key = getDate(o).format(fmt);
            if (dailyMap.containsKey(key)) {
                if (isPending(o))   dailyMap.get(key)[0]++;
                if (isCompleted(o)) dailyMap.get(key)[1]++;
            }
        });

        List<DailyOrdersDto> dailyOrders = dailyMap.entrySet().stream()
                .map(e -> new DailyOrdersDto(e.getKey(), e.getValue()[0], e.getValue()[1],
                        e.getValue()[0] + e.getValue()[1]))
                .collect(Collectors.toList());

        // ── Por estado ────────────────────────────────────────────────────────
        Map<String, Long> statusMap = periodOrders.stream()
                .collect(Collectors.groupingBy(o -> normalizeStatus(o.getOrderStatus()), Collectors.counting()));
        List<StatusCountDto> byStatus = statusMap.entrySet().stream()
                .map(e -> { StatusCountDto d = new StatusCountDto(e.getKey(), e.getValue().intValue());
                    d.setPercentage(totalInPeriod > 0 ? (e.getValue() * 100.0) / totalInPeriod : 0); return d; })
                .sorted(Comparator.comparingInt(StatusCountDto::getCount).reversed())
                .collect(Collectors.toList());

        // ── Pendientes para la tabla ──────────────────────────────────────────
        List<PendingOrderDto> pendingTable = allOrders.stream().filter(this::isPending)
                .sorted(Comparator.comparing(o -> getDate(o) != null ? getDate(o) : LocalDateTime.MIN, Comparator.reverseOrder()))
                .limit(10)
                .map(o -> {
                    String customer = o.getShippingAddress() != null
                            ? (o.getShippingAddress().getFirstName() + " " + o.getShippingAddress().getLastName()).trim()
                            : "Cliente";
                    String dateStr = getDate(o) != null ? getDate(o).format(fmt) : "N/A";
                    double total   = o.getTotalDiscountedPrice() != null && o.getTotalDiscountedPrice() > 0
                            ? o.getTotalDiscountedPrice() : o.getTotalPrice();
                    return new PendingOrderDto("#" + o.getId(), customer, total, normalizeStatus(o.getOrderStatus()), dateStr);
                })
                .collect(Collectors.toList());

        // ── Stock bajo ────────────────────────────────────────────────────────
        List<LowStockAlertDto> lowStock = productVariantRepository
                .findLowStockVariantsPaged(org.springframework.data.domain.PageRequest.of(0, 5))
                .getContent().stream()
                .map(v -> {
                    String name = v.getProduct().getTitle()
                            + (v.getColor() != null ? " " + v.getColor().getNombre() : "")
                            + (v.getTalla() != null ? " " + v.getTalla().getValor() : "");
                    String cat  = v.getProduct().getCategoria() != null ? v.getProduct().getCategoria().getName() : "General";
                    return new LowStockAlertDto(v.getId(), name.trim(), v.getStock(), cat);
                })
                .collect(Collectors.toList());

        // ── Armar respuesta ───────────────────────────────────────────────────
        EmployeeDashboardResponse resp = new EmployeeDashboardResponse();
        resp.setPendingOrders(pendingCount);
        resp.setProcessedToday(completedCount);
        resp.setTotalOrdersInPeriod(totalInPeriod);
        resp.setLowStockAlerts((int) lowStockCount);
        resp.setAvgProcessingRevenue(avgRevenue);
        resp.setProcessingRate(processingRate);
        resp.setDailyOrders(dailyOrders);
        resp.setOrdersByStatus(byStatus);
        resp.setPendingOrdersList(pendingTable);
        resp.setLowStockProducts(lowStock);
        return resp;
    }
}
