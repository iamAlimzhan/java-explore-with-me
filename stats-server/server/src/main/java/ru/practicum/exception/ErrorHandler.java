package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler(DateException.class)
    @ResponseStatus(BAD_REQUEST)
    public ApiError dateException(final DateException e) {
        log.warn("Произошла ошибка DateException со статусом 400 {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Некорректные даты")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
