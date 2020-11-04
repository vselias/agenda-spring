package com.example.app.validators;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

@GroupSequence(value = { NotBlank.class, Length.class, UniqueEmail.class})
public interface OrdemMensagem {

}
