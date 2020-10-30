package com.example.app.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.entidade.Pessoa;
@Repository
@Transactional
public interface PessoaDAO extends PagingAndSortingRepository<Pessoa, Long> {

	@Query("SELECT p from Pessoa p where p.email = :email")
	public Pessoa buscarPessoaPorEmail(@Param("email") String email);
	
	@Query("SELECT p from Usuario u join u.pessoas p where u.id = :id")
	public Page<Pessoa> buscarTodosPorUsuarioPag(@Param("id") Long id, Pageable pageable);
	
	@Query("SELECT p from Pessoa p join p.usuario u where u.id = :id")
	public Page<Pessoa> buscarTodosPorUsuarioOrdenacao(@Param("id") Long id, Pageable pageable);
	
	@Query("SELECT p from Pessoa p join p.usuario u where u.id = :id")
	public List<Pessoa> buscarTodosPorUsuario(@Param("id") Long id);
	
	@Query("SELECT p from Pessoa p join p.usuario u where u.id = :id and lower(p.nome) like lower(concat('%', :texto ,'%'))")
	public Page<Pessoa> buscarPorNome(@Param("texto") String texto, @Param("id") Long id ,Pageable pageable);

}
