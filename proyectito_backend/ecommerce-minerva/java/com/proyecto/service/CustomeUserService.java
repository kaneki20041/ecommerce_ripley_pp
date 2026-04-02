package com.proyecto.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.proyecto.model.Usuario;
import com.proyecto.repositories.UserRepository;

@Service
public class CustomeUserService implements UserDetailsService{

	private UserRepository userRepository;

	public CustomeUserService(UserRepository userRepository) {
		this.userRepository=userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{

		Usuario user=userRepository.findByEmail(username);
		if(user==null) {
			throw new UsernameNotFoundException("user not found with email ."+username);

		}

		List<GrantedAuthority> authorities=new ArrayList<>();


		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
	}
}
