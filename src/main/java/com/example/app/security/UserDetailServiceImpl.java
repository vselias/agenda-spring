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

import com.example.app.dao.PessoaDAO;
import com.example.app.entidade.Pessoa;

public class UserDetailServiceImpl implements UserDetailsService {
	@Autowired
	private PessoaDAO pessoaDAO;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Pessoa pessoa = pessoaDAO.buscarPessoaPorEmail(username);
		if (pessoa == null) {
			throw new UsernameNotFoundException("Could not find user");
		}
		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
		List<GrantedAuthority> lista = new ArrayList<>();
		lista.add(grantedAuthority);
		UserDetails userDetails = (UserDetails) new User(pessoa.getEmail(), pessoa.getSenha(), lista);
		return userDetails;
	}

}
