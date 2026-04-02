package com.proyecto.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.Product;
import com.proyecto.model.Review;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.ProductRepository;
import com.proyecto.repositories.ReviewRepository;
import com.proyecto.request.ReviewRequest;


@Service
public class ReviewServiceImplementation implements ReviewService{

	private ReviewRepository reviewRepository;
	private ProductService productservice;
	private ProductRepository productRepository;


	public ReviewServiceImplementation(
			ReviewRepository reviewRepository,
			ProductService productservice,
			ProductRepository productRepository) {
		this.productRepository=productRepository;
		this.reviewRepository=reviewRepository;
		this.productservice=productservice;
	}

	@Override
	public Review createReive(ReviewRequest req, Usuario user) throws ProductException {
		Product product=productservice.findProductById(req.getProductId());

		Review review=new Review();
		review.setUsuario(user);
		review.setProduct(product);
		review.setReview(req.getReview());
		review.setCreatedAt(LocalDateTime.now());

		return reviewRepository.save(review);
	}

	@Override
	public List<Review> getAllReview(Long productId) {

		return reviewRepository.getAllProductsReview(productId);
	}

}
