package com.reactivespring.globalerrorhandler;

import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(MoviesInfoClientException.class)
    public ResponseEntity<String> handleClientException(MoviesInfoClientException moviesInfoClientException){
        log.error("Exception caught in handleClientException : {} ", moviesInfoClientException.getMessage());
        return ResponseEntity.status(moviesInfoClientException.getStatusCode()).body(moviesInfoClientException.getMessage());

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleServerException(RuntimeException moviesInfoServerException){
        log.error("Exception caught in handleServerException : {} ", moviesInfoServerException.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(moviesInfoServerException.getMessage());

    }

}
