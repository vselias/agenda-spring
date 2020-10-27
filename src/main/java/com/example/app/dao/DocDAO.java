package com.example.app.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.entidade.Doc;
@Transactional
public interface DocDAO extends CrudRepository<Doc, Long>{

}
