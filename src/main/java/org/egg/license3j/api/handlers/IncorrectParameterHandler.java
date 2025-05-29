package org.egg.license3j.api.handlers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class IncorrectParameterHandler {
 
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, String>> handleViolation(ConstraintViolationException cve){
		Map<String, String> errors = new HashMap<>();
		for(ConstraintViolation<?> violation : cve.getConstraintViolations()) {
			errors.put(violation.getPropertyPath().toString(), violation.getMessage());
		}
		
		return ResponseEntity.badRequest().body(errors);
	}
}
