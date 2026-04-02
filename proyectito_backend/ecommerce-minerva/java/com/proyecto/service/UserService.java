package com.proyecto.service;

import java.util.List;

import com.proyecto.Exception.UserException;
import com.proyecto.model.Usuario;
import com.proyecto.request.AdminCreateUserRequest;
import com.proyecto.request.AdminUpdateUserRequest;
import com.proyecto.request.ChangePasswordRequest;
import com.proyecto.request.UpdateProfileRequest;

public interface UserService {

	public Usuario findUserProfileByJwt(String jwt) throws UserException;

	void changePassword(Long userId, ChangePasswordRequest request) throws UserException;

	void updateUserProfile(Long userId, UpdateProfileRequest request) throws UserException;

	public List<Usuario> getAllUsers();

	public void toggleUserStatus(Long userId) throws UserException;

	public void deleteUser(Long userId) throws UserException;

	public Usuario createUserByAdmin(AdminCreateUserRequest req) throws UserException;
	public Usuario updateUserByAdmin(Long userId, AdminUpdateUserRequest req) throws UserException;
}