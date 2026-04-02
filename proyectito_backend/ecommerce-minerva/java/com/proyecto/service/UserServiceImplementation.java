package com.proyecto.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.Exception.UserException;
import com.proyecto.model.Usuario;
import com.proyecto.repositories.UserRepository;
import com.proyecto.request.AdminCreateUserRequest;
import com.proyecto.request.AdminUpdateUserRequest;
import com.proyecto.request.ChangePasswordRequest;
import com.proyecto.request.UpdateProfileRequest;
import com.proyecto.security.JwtProvider;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;


    // Constructor para inyectar las dependencias
    public UserServiceImplementation(UserRepository userRepository, JwtProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }



    @Override
    public Usuario findUserProfileByJwt(String jwt) throws UserException {
        String email = jwtProvider.getEmailFromToken(jwt);

        Usuario user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UserException("Usuario no encontrado con email " + email);
        }
        return user;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) throws UserException {
    	Usuario user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Usuario no encontrado con id: " + userId));

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new UserException("La nueva contraseña no puede ser igual a la actual");
        }

        // Guardar la nueva contraseña cifrada
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public Usuario createUserByAdmin(AdminCreateUserRequest req) throws UserException {
        if (userRepository.findByEmail(req.getEmail()) != null) {
            throw new UserException("El email ya está registrado");
        }

        Usuario newUser = new Usuario();
        newUser.setFirstName(req.getFirstName());
        newUser.setLastName(req.getLastName());
        newUser.setEmail(req.getEmail());
        newUser.setCelular(req.getCelular());
        newUser.setPassword(passwordEncoder.encode(req.getPassword())); // Encriptamos

        // Convertimos el String del request al Enum
        if(req.getRole() != null) {
            newUser.setRole(Usuario.UserRole.valueOf(req.getRole().toUpperCase()));
        } else {
            newUser.setRole(Usuario.UserRole.USER); // Por defecto
        }

        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public void updateUserProfile(Long userId, UpdateProfileRequest request) throws UserException {
    	Usuario user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Usuario no encontrado con id: " + userId));

        user.setFoto(request.getFoto());
        user.setDireccion(request.getDireccion());
        user.setCelular(request.getCelular());
        user.setFechaNacimiento(request.getFechaNacimiento());

        userRepository.save(user);
    }

 // Agrega estos métodos dentro de tu UserServiceImplementation.java

    @Override
    public List<Usuario> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) throws UserException {
        Usuario user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Usuario no encontrado con id: " + userId));

        // Invierte el estado actual (Si es true pasa a false, si es false a true)
        user.setActive(!user.getActive());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) throws UserException {
        Usuario user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Usuario no encontrado con id: " + userId));

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public Usuario updateUserByAdmin(Long userId, AdminUpdateUserRequest req) throws UserException {
        Usuario user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("Usuario no encontrado con id: " + userId));

        if(req.getFirstName() != null) {
			user.setFirstName(req.getFirstName());
		}
        if(req.getLastName() != null) {
			user.setLastName(req.getLastName());
		}
        if(req.getEmail() != null) {
			user.setEmail(req.getEmail());
		}

        if(req.getRole() != null) {
            user.setRole(Usuario.UserRole.valueOf(req.getRole().toUpperCase()));
        }

        // Solo actualizamos la contraseña si el admin escribió una nueva
        if(req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        return userRepository.save(user);
    }
}
