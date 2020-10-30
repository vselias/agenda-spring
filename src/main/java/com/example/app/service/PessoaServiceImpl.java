package com.example.app.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.app.dao.PessoaDAO;
import com.example.app.dao.UserDAO;
import com.example.app.entidade.Doc;
import com.example.app.entidade.Pessoa;
import com.example.app.entidade.Usuario;

@Service
@Transactional
public class PessoaServiceImpl implements PessoaService {

	@Autowired
	private PessoaDAO pessoaDAO;
	@Autowired
	private UserDAO userDAO;

	@Override
	public void salvar(Pessoa pessoa, MultipartFile[] files) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Usuario user = userDAO.buscarUsuarioPorEmail(authentication.getName());
		if (pessoa.getId() != null) {
			Pessoa pessoaBusca = this.buscarPorId(pessoa.getId());
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
		user.getPessoas().add(pessoa);
		pessoa.setUsuario(user);
		pessoaDAO.save(pessoa);
	}

	@Override
	public List<Pessoa> buscarTodos() {
		return (List<Pessoa>) pessoaDAO.findAll();
	}

	@Override
	public Pessoa buscarPorId(long id) {
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
	public void remover(Pessoa pessoa) {
		pessoaDAO.delete(pessoa);
	}

	@Override
	public Page<Pessoa> buscarPorPaginacao(int page) {
		Pageable paginacao = PageRequest.of(page - 1, 5);
		Page<Pessoa> pessoas = pessoaDAO.findAll(paginacao);
		return pessoas;
	}

	@Override
	public Pessoa buscarPorEmail(String email) {
		return pessoaDAO.buscarPessoaPorEmail(email);
	}

	@Override
	public Page<Pessoa> sortPorTipo(String tipo, String ordem, int pag) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Usuario user = userDAO.buscarUsuarioPorEmail(authentication.getName());
		Sort sort = ordem.equalsIgnoreCase("asc") ? Sort.by(tipo).ascending() : Sort.by(tipo).descending();
		Pageable paginacao = PageRequest.of(pag - 1, 5, sort);
		Page<Pessoa> pessoas = pessoaDAO.buscarTodosPorUsuarioOrdenacao(user.getId(), paginacao);
		return pessoas;
	}

	@Override
	public List<Pessoa> buscarTodosPorUsuario(Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Usuario user = userDAO.buscarUsuarioPorEmail(authentication.getName());
		return pessoaDAO.buscarTodosPorUsuario(user.getId());
	}

	@Override
	public Page<Pessoa> buscarPaginacaoPorUsuario(int page) {
		Usuario usuario = userDAO
				.buscarUsuarioPorEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		Pageable paginacao = PageRequest.of(page - 1, 5);
		Page<Pessoa> pessoas = pessoaDAO.buscarTodosPorUsuarioPag(usuario.getId(), paginacao);
		return pessoas;
	}

}
