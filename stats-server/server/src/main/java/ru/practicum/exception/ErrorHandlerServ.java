package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ErrorHandlerServ {
    @ExceptionHandler(value = {ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MissingPathVariableException.class,
            DateTimeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResp handleValidationExceptions(Exception e) throws Exception {
        if (e instanceof ConstraintViolationException ||
                e instanceof MethodArgumentNotValidException ||
                e instanceof MissingPathVariableException ||
                e instanceof DateTimeException) {
            log.error("Произошла ошибка валидации со статусом 400: {}", e.getMessage(), e);
            return new ErrorResp("Ошибка валидации ", e.getMessage());
        }
        throw e;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResp handleMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        log.error("Произошла ошибка валидации: Не указан обязательный параметр запроса со статусом 400", e);
        return new ErrorResp("Ошибка валидации: Не указан обязательный параметр запроса", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResp handleThrowable(final Throwable e) {
        log.error("Произошла внутренняя ошибка сервера со статусом 500: {}", e.getMessage(), e);
        return new ErrorResp("INTERNAL_SERVER_ERROR", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResp handleRuntimeException(final RuntimeException e) {
        log.error("Произошла внутренняя ошибка сервера со статусом 500: {}", e.getMessage(), e);
        return new ErrorResp("INTERNAL_SERVER_ERROR", e.getMessage());
    }
}
