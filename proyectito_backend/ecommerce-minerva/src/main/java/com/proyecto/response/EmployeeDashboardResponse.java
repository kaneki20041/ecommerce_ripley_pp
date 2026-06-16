package com.proyecto.response;

import java.util.List;

/**
 * DTO de respuesta para el Dashboard Analítico del EMPLEADO.
 * Datos operacionales: pedidos pendientes, procesados hoy, eficiencia, etc.
 */
public class EmployeeDashboardResponse {

    // ─── KPIs Operacionales ───────────────────────────────────────────────────
    private int pendingOrders;           // Pedidos pendientes de acción
    private int processedToday;          // Pedidos procesados en el período
    private int totalOrdersInPeriod;     // Total pedidos en el período
    private int lowStockAlerts;          // Variantes con stock ≤ 5
    private double avgProcessingRevenue; // Ingreso promedio por pedido procesado
    private double processingRate;       // % pedidos completados vs totales

    // ─── Gráfica: Pedidos procesados por día ─────────────────────────────────
    private List<DailyOrdersDto> dailyOrders;

    // ─── Gráfica: Pedidos por estado ─────────────────────────────────────────
    private List<StatusCountDto> ordersByStatus;

    // ─── Tabla: Pedidos pendientes de atención ────────────────────────────────
    private List<PendingOrderDto> pendingOrdersList;

    // ─── Tabla: Productos con stock bajo ─────────────────────────────────────
    private List<LowStockAlertDto> lowStockProducts;

    public EmployeeDashboardResponse() {}

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public int getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }

    public int getProcessedToday() { return processedToday; }
    public void setProcessedToday(int processedToday) { this.processedToday = processedToday; }

    public int getTotalOrdersInPeriod() { return totalOrdersInPeriod; }
    public void setTotalOrdersInPeriod(int totalOrdersInPeriod) { this.totalOrdersInPeriod = totalOrdersInPeriod; }

    public int getLowStockAlerts() { return lowStockAlerts; }
    public void setLowStockAlerts(int lowStockAlerts) { this.lowStockAlerts = lowStockAlerts; }

    public double getAvgProcessingRevenue() { return avgProcessingRevenue; }
    public void setAvgProcessingRevenue(double avgProcessingRevenue) { this.avgProcessingRevenue = avgProcessingRevenue; }

    public double getProcessingRate() { return processingRate; }
    public void setProcessingRate(double processingRate) { this.processingRate = processingRate; }

    public List<DailyOrdersDto> getDailyOrders() { return dailyOrders; }
    public void setDailyOrders(List<DailyOrdersDto> dailyOrders) { this.dailyOrders = dailyOrders; }

    public List<StatusCountDto> getOrdersByStatus() { return ordersByStatus; }
    public void setOrdersByStatus(List<StatusCountDto> ordersByStatus) { this.ordersByStatus = ordersByStatus; }

    public List<PendingOrderDto> getPendingOrdersList() { return pendingOrdersList; }
    public void setPendingOrdersList(List<PendingOrderDto> pendingOrdersList) { this.pendingOrdersList = pendingOrdersList; }

    public List<LowStockAlertDto> getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(List<LowStockAlertDto> lowStockProducts) { this.lowStockProducts = lowStockProducts; }

    // ─── Inner DTOs ──────────────────────────────────────────────────────────

    public static class DailyOrdersDto {
        private String date;
        private int pending;
        private int completed;
        private int total;

        public DailyOrdersDto() {}
        public DailyOrdersDto(String date, int pending, int completed, int total) {
            this.date = date; this.pending = pending; this.completed = completed; this.total = total;
        }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }
        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
    }

    public static class StatusCountDto {
        private String status;
        private int count;
        private double percentage;

        public StatusCountDto() {}
        public StatusCountDto(String status, int count) { this.status = status; this.count = count; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public static class PendingOrderDto {
        private String orderId;
        private String customer;
        private double total;
        private String status;
        private String date;

        public PendingOrderDto() {}
        public PendingOrderDto(String orderId, String customer, double total, String status, String date) {
            this.orderId = orderId; this.customer = customer; this.total = total;
            this.status = status; this.date = date;
        }
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getCustomer() { return customer; }
        public void setCustomer(String customer) { this.customer = customer; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    public static class LowStockAlertDto {
        private Long variantId;
        private String name;
        private int stock;
        private String category;

        public LowStockAlertDto() {}
        public LowStockAlertDto(Long variantId, String name, int stock, String category) {
            this.variantId = variantId; this.name = name; this.stock = stock; this.category = category;
        }
        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}
