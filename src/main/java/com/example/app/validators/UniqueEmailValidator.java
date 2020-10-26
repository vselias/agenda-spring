package com.example.app.validators;

import javax.transaction.Transactional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.example.app.dao.PessoaDAO;

@Service
@Transactional
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

	@Autowired
	private PessoaDAO pessoaDAO;

	@Override
	public void initialize(UniqueEmail constraintAnnotation) {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		if(pessoaDAO == null) {
			return true;
		}
		return pessoaDAO.buscarPessoaPorEmail(email) == null;
	}

}
