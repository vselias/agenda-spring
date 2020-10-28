package com.example.app.validators;

import javax.transaction.Transactional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.example.app.dao.UserDAO;

@Service
@Transactional
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

//	@Autowired
//	private PessoaDAO pessoaDAO;
	@Autowired
	private UserDAO userDAO;

	@Override
	public void initialize(UniqueEmail constraintAnnotation) {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
//		if(pessoaDAO == null) {
//			return true;
//		}
//		return pessoaDAO.buscarPessoaPorEmail(email) == null;
		
		if(userDAO == null) {
			return true;
		}
		return userDAO.buscarUsuarioPorEmail(email) == null;
		
	}

}
