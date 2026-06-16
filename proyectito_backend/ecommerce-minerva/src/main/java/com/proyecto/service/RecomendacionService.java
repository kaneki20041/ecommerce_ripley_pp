package com.proyecto.service;

import com.proyecto.model.CartItem;
import com.proyecto.model.Usuario;
import com.proyecto.response.ProductCardResponse;

import java.util.List;

public interface RecomendacionService {
    
    List<ProductCardResponse> obtenerSugerenciasCarrito(List<CartItem> carritoActual, Usuario usuario, List<String> ignoredCategories);

}