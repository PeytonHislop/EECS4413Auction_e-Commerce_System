package com.auction.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return errors;
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleInvalidToken(InvalidTokenException ex) {

        return Map.of(
                "error", "Unauthorized",
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler(UserMismatchException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleUserMismatch(UserMismatchException ex) {

        return Map.of(
                "error", "Forbidden",
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleUnauthorizedRole(UnauthorizedRoleException ex) {

        return Map.of(
                "error", "Forbidden",
                "message", ex.getMessage()
        );
    }

}
