package org.example.booknest_2.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.booknest_2.controller.PdfRenderController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = PdfRenderController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Void> handleExternalService(ExternalServiceException e) {
        log.error("External service call failed", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleUnexpected(Exception e) {
        log.error("Unhandled exception while serving API request", e);
        return ResponseEntity.internalServerError().build();
    }
}
