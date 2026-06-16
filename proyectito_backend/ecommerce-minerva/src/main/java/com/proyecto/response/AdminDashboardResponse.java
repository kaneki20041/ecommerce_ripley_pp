package com.proyecto.response;

import java.util.List;

/**
 * DTO de respuesta para el Dashboard Analítico del ADMINISTRADOR.
 * Contiene métricas de negocio: ingresos, conversión, top categorías, etc.
 */
public class AdminDashboardResponse {

    // ─── KPIs Principales ────────────────────────────────────────────────────
    private double totalRevenue;
    private int totalOrders;
    private int totalUsers;
    private int totalProducts;
    private double avgOrderValue;
    private double conversionRate;       // (totalOrders / totalUsers) * 100
    private double revenueGrowth;        // % vs período anterior
    private double ordersGrowth;
    private double usersGrowth;

    // ─── Gráficas ────────────────────────────────────────────────────────────
    private List<DailySaleDto> dailySales;       // Barras: ventas últimos N días
    private List<CategoryRevenueDto> topCategories; // Pie: ingresos por categoría
    private List<StatusCountDto> ordersByStatus;    // Dona: pedidos por estado

    // ─── Tabla de últimos pedidos ─────────────────────────────────────────────
    private List<RecentOrderDto> recentOrders;

    // CTR de Recomendaciones de IA
    private double recommendationCtr;

    public AdminDashboardResponse() {}

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }

    public double getAvgOrderValue() { return avgOrderValue; }
    public void setAvgOrderValue(double avgOrderValue) { this.avgOrderValue = avgOrderValue; }

    public double getConversionRate() { return conversionRate; }
    public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }

    public double getRevenueGrowth() { return revenueGrowth; }
    public void setRevenueGrowth(double revenueGrowth) { this.revenueGrowth = revenueGrowth; }

    public double getOrdersGrowth() { return ordersGrowth; }
    public void setOrdersGrowth(double ordersGrowth) { this.ordersGrowth = ordersGrowth; }

    public double getUsersGrowth() { return usersGrowth; }
    public void setUsersGrowth(double usersGrowth) { this.usersGrowth = usersGrowth; }

    public List<DailySaleDto> getDailySales() { return dailySales; }
    public void setDailySales(List<DailySaleDto> dailySales) { this.dailySales = dailySales; }

    public List<CategoryRevenueDto> getTopCategories() { return topCategories; }
    public void setTopCategories(List<CategoryRevenueDto> topCategories) { this.topCategories = topCategories; }

    public List<StatusCountDto> getOrdersByStatus() { return ordersByStatus; }
    public void setOrdersByStatus(List<StatusCountDto> ordersByStatus) { this.ordersByStatus = ordersByStatus; }

    public List<RecentOrderDto> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<RecentOrderDto> recentOrders) { this.recentOrders = recentOrders; }

    public double getRecommendationCtr() { return recommendationCtr; }
    public void setRecommendationCtr(double recommendationCtr) { this.recommendationCtr = recommendationCtr; }

    // ─── Inner DTOs ──────────────────────────────────────────────────────────

    public static class DailySaleDto {
        private String date;    // "2024-05-01"
        private double revenue;
        private int orders;

        public DailySaleDto() {}
        public DailySaleDto(String date, double revenue, int orders) {
            this.date = date; this.revenue = revenue; this.orders = orders;
        }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
        public int getOrders() { return orders; }
        public void setOrders(int orders) { this.orders = orders; }
    }

    public static class CategoryRevenueDto {
        private String category;
        private double revenue;
        private int orders;
        private double percentage;

        public CategoryRevenueDto() {}
        public CategoryRevenueDto(String category, double revenue, int orders) {
            this.category = category; this.revenue = revenue; this.orders = orders;
        }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
        public int getOrders() { return orders; }
        public void setOrders(int orders) { this.orders = orders; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public static class StatusCountDto {
        private String status;
        private int count;
        private double percentage;

        public StatusCountDto() {}
        public StatusCountDto(String status, int count) {
            this.status = status; this.count = count;
        }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public static class RecentOrderDto {
        private String orderId;
        private String customer;
        private double total;
        private String status;
        private String date;
        private String paymentMethod;

        public RecentOrderDto() {}
        public RecentOrderDto(String orderId, String customer, double total, String status, String date, String paymentMethod) {
            this.orderId = orderId; this.customer = customer; this.total = total;
            this.status = status; this.date = date; this.paymentMethod = paymentMethod;
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
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }
}
