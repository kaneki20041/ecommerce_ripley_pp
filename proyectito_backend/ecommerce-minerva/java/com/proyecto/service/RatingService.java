package com.proyecto.service;

import java.util.List;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.Rating;
import com.proyecto.model.Usuario;
import com.proyecto.request.RatingRequest;

public interface RatingService {

	public Rating createRating(RatingRequest re,Usuario user) throws ProductException;
	public List<Rating>getProductsRating(Long productId);

}
