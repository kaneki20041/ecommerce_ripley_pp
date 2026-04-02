package com.proyecto.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.ProductException;
import com.proyecto.model.Product;
import com.proyecto.service.ProductService;

@RestController
@RequestMapping("/productito")
public class ProductController {

	@Autowired
	private ProductService productService;

    @GetMapping("/filter")
    public ResponseEntity<Page<Product>> filterProducts(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) List<String> color,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer minDiscount,
            @RequestParam(required = false, defaultValue = "desc") String sort,
            @RequestParam(required = false) String stock,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<Product> result = productService.getAllProduct(
                categoria, color, sizes, minPrice, maxPrice,
                minDiscount, sort, stock, pageNumber, pageSize);
        return ResponseEntity.ok(result);
    }


//    @PostMapping("/generate-sample")
//    public String generateSampleProducts() {
//        productService.generateSampleProducts(300);
//       return "150 productos generados exitosamente.";
//   }



	@GetMapping("/products/id/{productId}")
	public ResponseEntity<Product> findProductByIdHandler(@PathVariable Long productId) throws ProductException{

		Product product=productService.findProductById(productId);

		return new ResponseEntity<>(product,HttpStatus.ACCEPTED);
	}

    @GetMapping("/allproducts")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAllProducts();
        return ResponseEntity.ok(products);
    }
}
