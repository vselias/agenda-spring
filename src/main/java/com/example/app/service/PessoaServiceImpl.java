package com.example.app.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.app.dao.PessoaDAO;
import com.example.app.entidade.Doc;
import com.example.app.entidade.Pessoa;

@Service
public class PessoaServiceImpl implements PessoaService {

	@Autowired
	private PessoaDAO pessoaDAO;

	@Override
	public void salvar(Pessoa pessoa, MultipartFile[] files) {
		if (pessoa.getId() != null) {
			Pessoa pessoaBusca = this.findById(pessoa.getId());
			pessoa.setDocs(pessoaBusca.getDocs());
		}
		if (files != null && !files[0].isEmpty()) {
			for (MultipartFile file : files) {
				try {
					Doc doc = new Doc();
					doc.setNomeArquivo(file.getOriginalFilename());
					doc.setData(file.getBytes());
					doc.setTipoArquivo(file.getContentType());
					pessoa.getDocs().add(doc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		pessoa.setSenha(new BCryptPasswordEncoder().encode(pessoa.getSenha()));
		pessoaDAO.save(pessoa);
	}

	@Override
	public List<Pessoa> findAll() {
		return (List<Pessoa>) pessoaDAO.findAll();
	}

	@Override
	public Pessoa findById(long id) {
		Optional<Pessoa> optional = pessoaDAO.findById(id);
		Pessoa pessoa = null;
		try {
			if (optional.isPresent()) {
				pessoa = optional.get();
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao encontrar pessoa com id:" + id);
		}
		return pessoa;
	}

	@Override
	public void delete(Pessoa pessoa) {
		pessoaDAO.delete(pessoa);
	}

	@Override
	public Page<Pessoa> findPaging(int page) {
		Pageable paginacao = PageRequest.of(page - 1, 5);
		Page<Pessoa> pessoas = pessoaDAO.findAll(paginacao);
		return pessoas;
	}

	@Override
	public Pessoa buscarPessoaPorEmail(String email) {
		return pessoaDAO.buscarPessoaPorEmail(email);
	}

	@Override
	public Page<Pessoa> sortPorTipo(String tipo, String ordem, int pag) {
		Sort sort = ordem.equalsIgnoreCase("asc") ? Sort.by(tipo).ascending() : Sort.by(tipo).descending();
		Pageable paginacao = PageRequest.of(pag-1, 5, sort);
		Page<Pessoa> pessoas = pessoaDAO.findAll(paginacao);
		return pessoas;
	}

}
