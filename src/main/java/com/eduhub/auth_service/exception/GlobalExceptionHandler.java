package com.eduhub.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import com.eduhub.auth_service.exception.MfaRequiredException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage())));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage())));
    }

    @ExceptionHandler(InvalidOtpException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidOtp(InvalidOtpException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage())));
    }

    @ExceptionHandler(MfaRequiredException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleMfaRequired(MfaRequiredException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ErrorResponse(HttpStatus.ACCEPTED.value(), ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred")));
    }
}

class ErrorResponse {
    private final int status;
    private final String message;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}