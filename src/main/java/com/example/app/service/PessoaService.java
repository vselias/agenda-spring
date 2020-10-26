package com.example.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.example.app.entidade.Pessoa;

public interface PessoaService {
	
	void salvar(Pessoa pessoa, MultipartFile[] files);
	List<Pessoa> findAll();
	Pessoa findById(long id);
	void delete(Pessoa pessoa);
	Page<Pessoa> findPaging(int page);
	Pessoa buscarPessoaPorEmail(String email);
	Page<Pessoa> sortPorTipo(String tipo, String ordem, int pag);
}
