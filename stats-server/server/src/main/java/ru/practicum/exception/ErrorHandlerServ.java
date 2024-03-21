package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class ErrorHandlerServ {
    @ExceptionHandler(value = {ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MissingPathVariableException.class,
            DateTimeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResp handle(Exception e) throws Exception {
        if (e instanceof ConstraintViolationException ||
                e instanceof MethodArgumentNotValidException ||
                e instanceof MissingPathVariableException ||
                e instanceof DateTimeException) {
            return new ErrorResp("Ошибка валидации ", e.getMessage());
        }
        throw e;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResp handleMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        return new ErrorResp("Validation error: ", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResp handleThrowable(final Throwable e) {
        return new ErrorResp("INTERNAL_SERVER_ERROR", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResp handleRuntimeException(final RuntimeException e) {
        return new ErrorResp("INTERNAL_SERVER_ERROR", e.getMessage());
    }
}
