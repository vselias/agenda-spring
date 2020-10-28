package com.example.app.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.example.app.entidade.Usuario;

public interface UserDAO extends CrudRepository<Usuario, Long> {
	@Query("select u from Usuario u where u.email = :email")
	Usuario buscarUsuarioPorEmail(@Param("email") String email);
	
}
