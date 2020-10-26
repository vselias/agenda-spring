package com.example.app.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(value = { ElementType.FIELD })
@Constraint(validatedBy = { UniqueEmailValidator.class })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
	String message() default "{ATENÇÃO}: Email já cadastrado!";

	Class<?>[] groups() default {};

	public abstract Class<? extends Payload>[] payload() default {};

}
