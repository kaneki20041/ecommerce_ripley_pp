package com.proyecto.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.ProductException;
import com.proyecto.Exception.UserException;
import com.proyecto.model.Review;
import com.proyecto.model.Usuario;
import com.proyecto.request.ReviewRequest;
import com.proyecto.service.ReviewService;
import com.proyecto.service.UserService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private UserService userService;

	@PostMapping("/create")
	public ResponseEntity<Review> createReviewReview(@RequestBody ReviewRequest req,@RequestHeader ("Authorization") String jwt)throws UserException, ProductException{

		Usuario user=userService.findUserProfileByJwt(jwt);
		Review review=reviewService.createReive(req, user);

		return new ResponseEntity<>(review,HttpStatus.CREATED);
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<List<Review>> getProductsReview(@PathVariable Long productId) throws UserException, ProductException{

		List< Review> reviews=reviewService.getAllReview(productId);
		return new ResponseEntity<>(reviews,HttpStatus.ACCEPTED);
	}

}
