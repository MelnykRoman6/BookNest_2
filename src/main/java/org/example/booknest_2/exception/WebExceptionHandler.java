package org.example.booknest_2.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.booknest_2.controller.AuthController;
import org.example.booknest_2.controller.BookController;
import org.example.booknest_2.controller.CollectionController;
import org.example.booknest_2.controller.UserController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice(assignableTypes = {
        AuthController.class,
        BookController.class,
        CollectionController.class,
        UserController.class
})
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(ExternalServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public String handleExternalService(ExternalServiceException e) {
        log.error("External service call failed", e);
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpected(Exception e) {
        log.error("Unhandled exception while serving request", e);
        return "error/500";
    }
}
