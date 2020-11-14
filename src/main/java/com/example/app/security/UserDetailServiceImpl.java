package com.example.app.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.app.dao.UserDAO;
import com.example.app.entidade.Usuario;

public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	private UserDAO userDAO;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Usuario usuario = userDAO.buscarUsuarioPorEmail(username);
		if (usuario == null) {
			throw new UsernameNotFoundException("Could not find user");
		}

		List<GrantedAuthority> lista = new ArrayList<GrantedAuthority>();
		lista.add(new SimpleGrantedAuthority("ROLE_USER"));

		UserDetails userDetails = (UserDetails) new User(usuario.getEmail(), usuario.getSenha(), lista);
		return userDetails;
	}

}
