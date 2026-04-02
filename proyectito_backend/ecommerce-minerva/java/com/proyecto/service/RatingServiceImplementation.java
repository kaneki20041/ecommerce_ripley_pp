package com.proyecto.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.Product;
import com.proyecto.model.Rating;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.RatingRepository;
import com.proyecto.request.RatingRequest;

@Service
public class RatingServiceImplementation implements RatingService{

	private RatingRepository ratingRepository;
	private ProductService productService;

	public RatingServiceImplementation(RatingRepository ratingRepository,ProductService productService) {
		this.ratingRepository=ratingRepository;
		this.productService=productService;
	}

	@Override
	public Rating createRating(RatingRequest re, Usuario user) throws ProductException {

		Product product=productService.findProductById(re.getProductId());

		Rating rating=new Rating();
		rating.setProduct(product);
		rating.setUsuario(user);
		rating.setRating(re.getRating());
		rating.setCreatedAt(LocalDateTime.now());
		return ratingRepository.save(rating);
	}

	@Override
	public List<Rating> getProductsRating(Long productId) {
		return ratingRepository.getAllProductsRating(productId);
	}

}
