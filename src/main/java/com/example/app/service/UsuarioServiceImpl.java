package com.example.app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.dao.UserDAO;
import com.example.app.entidade.Usuario;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

	@Autowired
	private UserDAO userDAO;

	@Override
	public void salvar(Usuario usuario) {
		usuario.setSenha(new BCryptPasswordEncoder().encode(usuario.getSenha()));
		userDAO.save(usuario);
	}

	@Override
	public Usuario buscarPorId(Long id) {
		Optional<Usuario> findById = userDAO.findById(id);
		if (findById.isPresent()) {
			return findById.get();
		} else {
			throw new RuntimeException("Erro ao encontrar usuario id: " + id);
		}
	}

	@Override
	public void remover(Usuario usuario) {
		userDAO.delete(usuario);
	}

	@Override
	public List<Usuario> buscarTodos() {
		return (List<Usuario>) userDAO.findAll();
	}

	@Override
	public Usuario buscarUsuarioPorEmail(String email) {
		return userDAO.buscarUsuarioPorEmail(email);
	}

	@Override
	public Usuario buscarPorToken(String token) {
		return userDAO.buscarUsuarioPorToken(token);
	}


}
