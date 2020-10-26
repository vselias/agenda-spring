package com.example.app.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.app.entidade.Pessoa;
@Repository
public interface PessoaDAO extends PagingAndSortingRepository<Pessoa, Long> {

	@Query("SELECT p from Pessoa p where p.email = :email")
	public Pessoa buscarPessoaPorEmail(@Param("email") String email);

}
