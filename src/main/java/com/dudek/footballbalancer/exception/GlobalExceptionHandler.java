package com.dudek.footballbalancer.exception;

import com.dudek.footballbalancer.model.dto.ExceptionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserAlreadyLinkedException.class, SkillsMismatchException.class})
    ResponseEntity<ExceptionDto> conflictHandler(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ExceptionDto(e.getMessage()));
    }
}
