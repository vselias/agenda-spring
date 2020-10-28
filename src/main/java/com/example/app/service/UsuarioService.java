package com.example.app.service;

import java.util.List;

import com.example.app.entidade.Usuario;

public interface UsuarioService {

	void salvar(Usuario usuario);
	Usuario buscarPorId(Long id);
	void remover(Usuario usuario);
	List<Usuario> buscarTodos();
	Usuario buscarUsuarioPorEmail(String name);
}
