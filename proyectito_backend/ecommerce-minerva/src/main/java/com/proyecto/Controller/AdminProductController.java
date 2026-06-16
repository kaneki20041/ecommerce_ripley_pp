package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.ProductException;
import com.proyecto.Exception.UserException;
import com.proyecto.model.Product;
import com.proyecto.model.ProductVariant;
import com.proyecto.model.Usuario;
import com.proyecto.request.CreateProductRequest;
import com.proyecto.request.UpdateProductBasicRequest;
import com.proyecto.request.UpdateVariantRequest;
import com.proyecto.response.ApiResponse;
import com.proyecto.response.PaginatedResponse;
import com.proyecto.response.ProductAdminListResponse;
import com.proyecto.response.SingleVariantResponse;
import com.proyecto.service.ProductService;
import com.proyecto.service.UserService;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
@RequestMapping("/api/admin/products")
public class AdminProductController {

	@Autowired
	private ProductService productService;
	@Autowired
	private UserService userService;

	@PostMapping("/create")
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @RequestBody CreateProductRequest req,
            @RequestHeader("Authorization") String jwt) {

        try {

            Usuario adminUser = userService.findUserProfileByJwt(jwt);

            System.out.println("El usuario " + adminUser.getEmail() + " está creando un producto.");

            Product createdProduct = productService.createProduct(req, adminUser);

            ApiResponse<Product> response = new ApiResponse<>();
            response.setMessage("Producto y variantes creados exitosamente");
            response.setResult(true);
            response.setData(createdProduct);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            ApiResponse<Product> response = new ApiResponse<>();
            response.setMessage("Error al crear el producto: " + e.getMessage());
            response.setResult(false);

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	@PutMapping("/{productId}/basic-info")
    public ResponseEntity<ApiResponse<Product>> updateProductBasicInfo(
            @PathVariable Long productId,
            @RequestBody UpdateProductBasicRequest req,
            @RequestHeader("Authorization") String jwt) throws ProductException, UserException {

        userService.findUserProfileByJwt(jwt);

        Product updatedProduct = productService.updateProductBasicInfo(productId, req);

        ApiResponse<Product> res = new ApiResponse<>();
        res.setMessage("Información básica del producto actualizada con éxito");
        res.setResult(true);
        res.setData(updatedProduct);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

	@PatchMapping("/variants/{variantId}/toggle-status")
    public ResponseEntity<ApiResponse<String>> toggleVariantStatus(
            @PathVariable Long variantId,
            @RequestHeader("Authorization") String jwt) throws ProductException, UserException {

        userService.findUserProfileByJwt(jwt);

        // Llamamos al nuevo método
        String mensaje = productService.toggleVariantStatus(variantId);

        ApiResponse<String> res = new ApiResponse<>();
        res.setMessage(mensaje);
        res.setResult(true);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

	@GetMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<SingleVariantResponse>> getVariantById(
            @PathVariable Long variantId,
            @RequestHeader("Authorization") String jwt) throws ProductException, UserException {

        // Validamos el token
        userService.findUserProfileByJwt(jwt);

        // Buscamos la variante
        ProductVariant variant = productService.getVariantById(variantId);

        // LA MAGIA: Fusionamos la variante con su producto padre usando nuestro nuevo DTO
        SingleVariantResponse responseDTO = new SingleVariantResponse(variant.getProduct(), variant);

        // Armamos la respuesta
        ApiResponse<SingleVariantResponse> res = new ApiResponse<>();
        res.setMessage("Datos de la variante obtenidos correctamente");
        res.setResult(true);
        res.setData(responseDTO); // Enviamos el DTO fusionado

        return new ResponseEntity<>(res, HttpStatus.OK);
    }


    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductAdminListResponse>>> findAllProduct(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) { // Aquí puedes recibir 10, 15 o 20

        try {
            // Validamos seguridad
            userService.findUserProfileByJwt(jwt);

            // Obtenemos la data ya paginada y lista
            PaginatedResponse<ProductAdminListResponse> paginatedData = productService.getPaginatedVariants(page, size);

            ApiResponse<PaginatedResponse<ProductAdminListResponse>> response = new ApiResponse<>();
            response.setMessage("Inventario paginado obtenido correctamente");
            response.setResult(true);
            response.setData(paginatedData);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponse<PaginatedResponse<ProductAdminListResponse>> response = new ApiResponse<>();
            response.setMessage("Error al obtener el inventario: " + e.getMessage());
            response.setResult(false);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariant>> updateVariant(
            @PathVariable Long variantId,
            @RequestBody UpdateVariantRequest req,
            @RequestHeader("Authorization") String jwt) throws ProductException, UserException {

        // Validar seguridad
        userService.findUserProfileByJwt(jwt);

        // Ejecutar la actualización
        ProductVariant updatedVariant = productService.updateVariant(variantId, req);

        // Armar respuesta
        ApiResponse<ProductVariant> res = new ApiResponse<>();
        res.setMessage("Variante actualizada correctamente sin afectar al resto del producto");
        res.setResult(true);
        res.setData(updatedVariant);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
