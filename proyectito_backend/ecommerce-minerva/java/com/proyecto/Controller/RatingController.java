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
import com.proyecto.model.Rating;
import com.proyecto.model.Usuario;
import com.proyecto.request.RatingRequest;
import com.proyecto.service.RatingService;
import com.proyecto.service.UserService;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

	@Autowired
	private UserService userService;

	@Autowired
	private RatingService ratingService;

	@PostMapping("/create")
	public ResponseEntity<Rating> createRating(@RequestBody RatingRequest req,
			@RequestHeader("Authorization")String jwt)throws UserException,ProductException{

		Usuario user=userService.findUserProfileByJwt(jwt);
		Rating rating=ratingService.createRating(req, user);
		return new ResponseEntity<>(rating,HttpStatus.CREATED);
	}
	@GetMapping("/product/{productid}")
	public ResponseEntity<List<Rating>> getProductsRating(@PathVariable Long productId,@RequestHeader("Authorization")String jwt)throws UserException, ProductException{
		Usuario user=userService.findUserProfileByJwt(jwt);

		List<Rating> ratings=ratingService.getProductsRating(productId);
		return new ResponseEntity<>(ratings,HttpStatus.CREATED);
	}

}
