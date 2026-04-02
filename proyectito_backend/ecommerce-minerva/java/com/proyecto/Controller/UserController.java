package com.proyecto.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.UserException;
import com.proyecto.model.Usuario;
import com.proyecto.request.ChangePasswordRequest;
import com.proyecto.request.UpdateProfileRequest;
import com.proyecto.response.ApiResponse;
import com.proyecto.response.UsuarioResponse;
import com.proyecto.service.UserService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UsuarioResponse>> getUserProfileHandler(@RequestHeader("Authorization") String jwt) {
        try {
            Usuario user = userService.findUserProfileByJwt(jwt);
            UsuarioResponse userDto = new UsuarioResponse(user);

            ApiResponse<UsuarioResponse> response = new ApiResponse<>();
            response.setResult(true);
            response.setMessage("Perfil obtenido correctamente.");
            response.setData(userDto);

            return ResponseEntity.ok(response);

        } catch (UserException e) {
            ApiResponse<UsuarioResponse> response = new ApiResponse<>();
            response.setResult(false);
            response.setMessage(e.getMessage());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            ApiResponse<UsuarioResponse> response = new ApiResponse<>();
            response.setResult(false);
            response.setMessage("Error inesperado al obtener el perfil.");
            response.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestHeader("Authorization") String jwt,
            @Valid @RequestBody ChangePasswordRequest request,
            BindingResult result) {

        // Validación de campos vacíos con @Valid
        if (result.hasErrors()) {
            String errorMessage = result.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest().body(new ApiResponse<>(errorMessage, false));
        }

        // Validación de coincidencia entre contraseñas
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Las contraseñas no coinciden", false));
        }

        try {
            Usuario user = userService.findUserProfileByJwt(jwt);

            userService.changePassword(user.getId(), request);
            return ResponseEntity.ok(new ApiResponse<>("Contraseña cambiada exitosamente", true));
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage(), false));
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @RequestHeader("Authorization") String jwt,
            @RequestBody UpdateProfileRequest request) {

        try {
        	// 1. Extraemos el usuario desde el JWT
            Usuario user = userService.findUserProfileByJwt(jwt);

            // 2. Pasamos el ID limpio al servicio
            userService.updateUserProfile(user.getId(), request);
            return ResponseEntity.ok(new ApiResponse<>("Perfil actualizado correctamente", true));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), false));
        }
    }



}
