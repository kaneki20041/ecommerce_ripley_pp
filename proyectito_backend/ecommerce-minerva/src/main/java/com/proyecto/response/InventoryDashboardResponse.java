package com.proyecto.response;

import java.util.List;

public class InventoryDashboardResponse {
	private long activeProductsCount;
	private long pendingOrdersCount;
	private long lowStockProductsCount;
	private List<PendingOrderDto> pendingOrders;
	private List<LowStockProductDto> lowStockProducts;

	public InventoryDashboardResponse() {
	}

	public InventoryDashboardResponse(long activeProductsCount, long pendingOrdersCount, long lowStockProductsCount,
			List<PendingOrderDto> pendingOrders, List<LowStockProductDto> lowStockProducts) {
		this.activeProductsCount = activeProductsCount;
		this.pendingOrdersCount = pendingOrdersCount;
		this.lowStockProductsCount = lowStockProductsCount;
		this.pendingOrders = pendingOrders;
		this.lowStockProducts = lowStockProducts;
	}

	public long getActiveProductsCount() { return activeProductsCount; }
	public void setActiveProductsCount(long activeProductsCount) { this.activeProductsCount = activeProductsCount; }

	public long getPendingOrdersCount() { return pendingOrdersCount; }
	public void setPendingOrdersCount(long pendingOrdersCount) { this.pendingOrdersCount = pendingOrdersCount; }

	public long getLowStockProductsCount() { return lowStockProductsCount; }
	public void setLowStockProductsCount(long lowStockProductsCount) { this.lowStockProductsCount = lowStockProductsCount; }

	public List<PendingOrderDto> getPendingOrders() { return pendingOrders; }
	public void setPendingOrders(List<PendingOrderDto> pendingOrders) { this.pendingOrders = pendingOrders; }

	public List<LowStockProductDto> getLowStockProducts() { return lowStockProducts; }
	public void setLowStockProducts(List<LowStockProductDto> lowStockProducts) { this.lowStockProducts = lowStockProducts; }

	// ─── Inner DTOs ──────────────────────────────────────────────────────────

	public static class PendingOrderDto {
		private String id;
		private String customer;
		private double total;
		private String status;
		private String date;

		public PendingOrderDto() {}

		public PendingOrderDto(String id, String customer, double total, String status, String date) {
			this.id = id;
			this.customer = customer;
			this.total = total;
			this.status = status;
			this.date = date;
		}

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public String getCustomer() { return customer; }
		public void setCustomer(String customer) { this.customer = customer; }
		public double getTotal() { return total; }
		public void setTotal(double total) { this.total = total; }
		public String getStatus() { return status; }
		public void setStatus(String status) { this.status = status; }
		public String getDate() { return date; }
		public void setDate(String date) { this.date = date; }
	}

	public static class LowStockProductDto {
		private Long variantId;   // ← ID de la variante para poder hacer restock
		private String sku;       // ← SKU para mostrar en el modal
		private String name;
		private int stock;
		private String category;
		private int price;        // ← Precio actual para referencia

		public LowStockProductDto() {}

		public LowStockProductDto(Long variantId, String sku, String name, int stock, String category, int price) {
			this.variantId = variantId;
			this.sku = sku;
			this.name = name;
			this.stock = stock;
			this.category = category;
			this.price = price;
		}

		public Long getVariantId() { return variantId; }
		public void setVariantId(Long variantId) { this.variantId = variantId; }
		public String getSku() { return sku; }
		public void setSku(String sku) { this.sku = sku; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public int getStock() { return stock; }
		public void setStock(int stock) { this.stock = stock; }
		public String getCategory() { return category; }
		public void setCategory(String category) { this.category = category; }
		public int getPrice() { return price; }
		public void setPrice(int price) { this.price = price; }
	}
}
