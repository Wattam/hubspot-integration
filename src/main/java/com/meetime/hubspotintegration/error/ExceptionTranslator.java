package com.meetime.hubspotintegration.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ExceptionTranslator {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(BadRequestException ex) {
         return new ResponseEntity<>(
                 new ErrorDetails(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), LocalDateTime.now()),
                 HttpStatus.BAD_REQUEST
         );
    }

    @ExceptionHandler(HubSpotServerErrorException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(HubSpotServerErrorException ex) {
         return new ResponseEntity<>(
                 new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), LocalDateTime.now()),
                 HttpStatus.INTERNAL_SERVER_ERROR
         );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGenericException() {
         ErrorDetails errorDetails = new ErrorDetails(
                 HttpStatus.INTERNAL_SERVER_ERROR.value(),
                 "An unexpected error occurred.",
                 LocalDateTime.now()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
