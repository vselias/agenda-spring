package com.example.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.example.app.entidade.Pessoa;

public interface PessoaService {
	
	void salvar(Pessoa pessoa, MultipartFile[] files);
	List<Pessoa> buscarTodos();
	Pessoa buscarPorId(long id);
	void remover(Pessoa pessoa);
	Page<Pessoa> buscarPorPaginacao(int page);
	Page<Pessoa> buscarPaginacaoPorUsuario(int page);
	Pessoa buscarPorEmail(String email);
	Page<Pessoa> sortPorTipo(String tipo, String ordem, int pag);
	List<Pessoa> buscarTodosPorUsuario();
	Page<Pessoa> buscarPorNome(String texto, Long id);
}
