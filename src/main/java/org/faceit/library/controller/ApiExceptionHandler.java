package org.faceit.library.controller;

import jakarta.persistence.EntityNotFoundException;
import org.faceit.library.service.exception.BookNotFoundException;
import org.faceit.library.service.exception.S3Exception;
import org.faceit.library.service.exception.UserAlreadyExistException;
import org.faceit.library.service.exception.UserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {
            UserNotFoundException.class,
            UsernameNotFoundException.class,
            BookNotFoundException.class,
            EntityNotFoundException.class,
    })
    public ResponseEntity<Object> handleNotFound(Exception exception, WebRequest request) {
        Map<String, String> body = new HashMap<>();
        body.put("message", exception.getMessage());
        body.put("code", String.valueOf(HttpStatus.NOT_FOUND.value()));
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {
            S3Exception.class,
            IllegalArgumentException.class,
            AuthenticationException.class,
            UserAlreadyExistException.class
    })
    public ResponseEntity<Object> handleBadRequest(Exception exception, WebRequest request) {
        Map<String, String> body = new HashMap<>();
        body.put("message", exception.getMessage());
        body.put("code", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}
