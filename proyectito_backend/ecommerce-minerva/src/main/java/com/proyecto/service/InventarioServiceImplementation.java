package com.proyecto.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.proyecto.model.Order;
import com.proyecto.model.ProductVariant;
import com.proyecto.repositories.OrderRepository;
import com.proyecto.repositories.ProductRepository;
import com.proyecto.repositories.ProductVariantRepository;
import com.proyecto.response.InventoryDashboardResponse;

@Service
public class InventarioServiceImplementation implements InventarioService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductVariantRepository productVariantRepository;

	@Autowired
	private OrderRepository orderRepository;

	// ─── Helper: mapear ProductVariant → LowStockProductDto ─────────────────
	private InventoryDashboardResponse.LowStockProductDto toDto(ProductVariant v) {
		String colorName = v.getColor() != null ? " " + v.getColor().getNombre() : "";
		String sizeVal   = v.getTalla()  != null ? " " + v.getTalla().getValor()  : "";
		String fullName  = (v.getProduct().getTitle() + colorName + sizeVal).trim();
		String catName   = v.getProduct().getCategoria() != null
				? v.getProduct().getCategoria().getName() : "General";
		return new InventoryDashboardResponse.LowStockProductDto(
				v.getId(),
				v.getSku(),
				fullName,
				v.getStock(),
				catName,
				v.getDiscountedPrice() != 0 ? v.getDiscountedPrice() : v.getPrice()
		);
	}

	// ─── Stats generales del dashboard ──────────────────────────────────────
	@Override
	public InventoryDashboardResponse getInventoryDashboardStats() {
		long activeProductsCount = productRepository.count();

		List<Order> allOrders = orderRepository.findAll();
		List<Order> pendingOrdersList = allOrders.stream()
				.filter(o -> o.getOrderStatus() != null &&
						(o.getOrderStatus().equalsIgnoreCase("PENDING") ||
						 o.getOrderStatus().equalsIgnoreCase("PLACED") ||
						 o.getOrderStatus().equalsIgnoreCase("CONFIRMED") ||
						 o.getOrderStatus().equalsIgnoreCase("PROCESSING") ||
						 o.getOrderStatus().equalsIgnoreCase("PROCESO")))
				.collect(Collectors.toList());
		long pendingOrdersCount = pendingOrdersList.size();

		// Total de variantes con stock bajo (para el contador de la tarjeta)
		long lowStockProductsCount = productVariantRepository.countLowStockVariants();

		// Primeras 5 variantes con stock más bajo para el dashboard
		Pageable firstPage = PageRequest.of(0, 5);
		Page<ProductVariant> firstPageVariants = productVariantRepository.findLowStockVariantsPaged(firstPage);
		List<InventoryDashboardResponse.LowStockProductDto> lowStockProductsList = firstPageVariants
				.getContent().stream().map(this::toDto).collect(Collectors.toList());

		// Últimos 10 pedidos pendientes
		List<InventoryDashboardResponse.PendingOrderDto> pendingOrders = pendingOrdersList.stream()
				.sorted((o1, o2) -> {
					if (o2.getCreateAt() != null && o1.getCreateAt() != null)
						return o2.getCreateAt().compareTo(o1.getCreateAt());
					return 0;
				})
				.limit(10)
				.map(o -> {
					String customerName = "Cliente";
					if (o.getShippingAddress() != null)
						customerName = (o.getShippingAddress().getFirstName() + " " + o.getShippingAddress().getLastName()).trim();
					String dateStr = o.getCreateAt() != null
							? o.getCreateAt().toString().split("T")[0]
							: (o.getOrderDate() != null ? o.getOrderDate().toString().split("T")[0] : "Hoy");
					double total = o.getTotalDiscountedPrice() != 0 ? o.getTotalDiscountedPrice() : o.getTotalPrice();
					return new InventoryDashboardResponse.PendingOrderDto(
							"#" + o.getId(), customerName, total, o.getOrderStatus().toLowerCase(), dateStr);
				}).collect(Collectors.toList());

		return new InventoryDashboardResponse(
				activeProductsCount, pendingOrdersCount, lowStockProductsCount, pendingOrders, lowStockProductsList);
	}

	// ─── Lista paginada de variantes con stock bajo ──────────────────────────
	@Override
	public Page<InventoryDashboardResponse.LowStockProductDto> getLowStockProductsPaged(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductVariant> variantsPage = productVariantRepository.findLowStockVariantsPaged(pageable);
		List<InventoryDashboardResponse.LowStockProductDto> dtoList = variantsPage
				.getContent().stream().map(this::toDto).collect(Collectors.toList());
		return new PageImpl<>(dtoList, pageable, variantsPage.getTotalElements());
	}

	// ─── Restock: suma cantidad al stock de una variante ────────────────────
	@Override
	public int restockVariant(Long variantId, int cantidad) {
		if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
		ProductVariant variant = productVariantRepository.findById(variantId)
				.orElseThrow(() -> new RuntimeException("Variante con ID " + variantId + " no encontrada"));
		variant.setStock(variant.getStock() + cantidad);
		productVariantRepository.save(variant);
		return variant.getStock();
	}
}
