package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.response.ApiResponse;
import com.proyecto.response.InventoryDashboardResponse;
import com.proyecto.service.InventarioService;

@RestController
@RequestMapping("/api/admin/inventario")
public class InventarioController {

	@Autowired
	private InventarioService inventarioService;

	/**
	 * GET /api/admin/inventario/stats
	 * Estadísticas generales: productos activos, pedidos pendientes,
	 * total de variantes con stock bajo y primeras 5 para el dashboard.
	 */
	@GetMapping("/stats")
	public ResponseEntity<ApiResponse<InventoryDashboardResponse>> getInventoryDashboardStats() {
		InventoryDashboardResponse stats = inventarioService.getInventoryDashboardStats();
		return ResponseEntity.ok(new ApiResponse<>("Estadísticas de inventario recuperadas con éxito", true, stats));
	}

	/**
	 * GET /api/admin/inventario/low-stock?page=0&size=5
	 * Lista paginada de variantes con stock bajo (≤ 5),
	 * ordenadas de menor a mayor stock (0, 1, 2 …).
	 */
	@GetMapping("/low-stock")
	public ResponseEntity<ApiResponse<Page<InventoryDashboardResponse.LowStockProductDto>>> getLowStockPaged(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size) {
		Page<InventoryDashboardResponse.LowStockProductDto> result = inventarioService.getLowStockProductsPaged(page, size);
		return ResponseEntity.ok(new ApiResponse<>("Productos con stock bajo recuperados con éxito", true, result));
	}

	/**
	 * PUT /api/admin/inventario/restock/{variantId}?cantidad=10
	 * Suma `cantidad` unidades al stock actual de la variante especificada.
	 * Retorna el nuevo stock total tras el restock.
	 */
	@PutMapping("/restock/{variantId}")
	public ResponseEntity<ApiResponse<Integer>> restockVariant(
			@PathVariable Long variantId,
			@RequestParam int cantidad) {
		try {
			int newStock = inventarioService.restockVariant(variantId, cantidad);
			return ResponseEntity.ok(
					new ApiResponse<>("Stock repuesto con éxito. Nuevo stock: " + newStock, true, newStock));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(e.getMessage(), false, 0));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse<>(e.getMessage(), false, 0));
		}
	}
}
