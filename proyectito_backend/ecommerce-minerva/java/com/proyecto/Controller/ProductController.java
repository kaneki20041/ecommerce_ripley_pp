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
import com.proyecto.response.ApiResponse;
import com.proyecto.response.PaginatedResponse;
import com.proyecto.response.ProductCardResponse;
import com.proyecto.service.ProductService;

@RestController
@RequestMapping("api/products/public")
public class ProductController {

	@Autowired
	private ProductService productService;

	@GetMapping("/filter")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductCardResponse>>> getFilteredProducts(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) Boolean isNuevo,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer minDiscount,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String stock,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "12") Integer pageSize) { // 12 productos recomendados

		// 1. Obtenemos la data paginada y filtrada
        PaginatedResponse<ProductCardResponse> paginatedData = productService.getAllProductPublic(
                categoria, genero, isNuevo, colors, sizes, minPrice, maxPrice, 
                minDiscount, sort, stock, pageNumber, pageSize);

        // 2. Lo envolvemos en tu ApiResponse
        ApiResponse<PaginatedResponse<ProductCardResponse>> apiResponse = 
            new ApiResponse<>("Productos obtenidos exitosamente", true, paginatedData);

        // 3. Lo retornamos
        return ResponseEntity.ok(apiResponse);
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
