package com.proyecto.Controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.Exception.UserException;
import com.proyecto.model.Usuario;
import com.proyecto.request.AdminCreateUserRequest;
import com.proyecto.request.AdminUpdateUserRequest;
import com.proyecto.response.AdminUsuarioResponse;
import com.proyecto.response.ApiResponse;
import com.proyecto.service.UserService;

@RestController
@RequestMapping("/admin/gestion-usuarios")
@Validated
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
		this.userService = userService;
		}

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AdminUsuarioResponse>> createUser(
            @RequestBody AdminCreateUserRequest req,
            @RequestHeader("Authorization") String jwt) {
        try {
            Usuario createdUser = userService.createUserByAdmin(req);
            AdminUsuarioResponse responseDto = new AdminUsuarioResponse(createdUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Usuario creado correctamente", true, responseDto)
            );
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), false));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AdminUsuarioResponse>>> getAllUsers(
            @RequestHeader("Authorization") String jwt) {
        try {
            // Opcional: Validar que quien pide esto sea realmente un admin
            Usuario adminUser = userService.findUserProfileByJwt(jwt);

            // Obtener todos los usuarios y convertirlos a DTO
            List<Usuario> users = userService.getAllUsers();
            List<AdminUsuarioResponse> usersDto = users.stream()
                .map(AdminUsuarioResponse::new)
                .collect(Collectors.toList());

            ApiResponse<List<AdminUsuarioResponse>> response = new ApiResponse<>();
            response.setResult(true);
            response.setMessage("Lista de usuarios obtenida exitosamente");
            response.setData(usersDto);

            return ResponseEntity.ok(response);

        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(e.getMessage(), false));
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<String>> toggleUserStatus(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String jwt) {
        try {
            userService.toggleUserStatus(userId);
            return ResponseEntity.ok(new ApiResponse<>("Estado actualizado correctamente", true));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(e.getMessage(), false));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String jwt) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ApiResponse<>("Usuario eliminado correctamente", true));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(e.getMessage(), false));
        }
    }
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUsuarioResponse>> updateUser(
            @PathVariable Long userId,
            @RequestBody AdminUpdateUserRequest req,
            @RequestHeader("Authorization") String jwt) {
        try {
            Usuario updatedUser = userService.updateUserByAdmin(userId, req);
            AdminUsuarioResponse responseDto = new AdminUsuarioResponse(updatedUser);

            return ResponseEntity.ok(new ApiResponse<>(
                "Usuario actualizado correctamente", true, responseDto
            ));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(e.getMessage(), false));
        }
    }
    private Authentication createAuthentication(Usuario user) {
        String role = "ROLE_" + user.getRole().name();
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(role)
        );

        return new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
    }
}
