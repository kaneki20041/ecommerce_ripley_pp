package com.proyecto.service;

import org.springframework.data.domain.Page;

import com.proyecto.response.InventoryDashboardResponse;

public interface InventarioService {
	InventoryDashboardResponse getInventoryDashboardStats();
	Page<InventoryDashboardResponse.LowStockProductDto> getLowStockProductsPaged(int page, int size);

	/**
	 * Repone el stock de una variante específica sumando la cantidad indicada.
	 * @param variantId  ID de la variante a reponer
	 * @param cantidad   Unidades a añadir al stock actual (debe ser > 0)
	 * @return Stock nuevo de la variante tras el restock
	 */
	int restockVariant(Long variantId, int cantidad);
}
