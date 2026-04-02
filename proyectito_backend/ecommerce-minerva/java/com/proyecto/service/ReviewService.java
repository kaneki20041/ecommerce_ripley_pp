package com.proyecto.service;

import java.util.List;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.Review;
import com.proyecto.model.Usuario;
import com.proyecto.request.ReviewRequest;

public interface ReviewService {

	public Review createReive(ReviewRequest req, Usuario user)throws ProductException;
	public List<Review>getAllReview(Long productId);
}
