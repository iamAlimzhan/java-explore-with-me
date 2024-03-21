package ru.practicum.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.ErrorRequestException;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MissingPathVariableException.class,
            JsonException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(Exception e) {
        log.error("Произошла ошибка валидации статус 400: {}", e.getMessage(), e);
        return new ErrorResponse("Ошибка валидации: ", e.getMessage());
    }

    @ExceptionHandler({ErrorRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRuntimeException(final RuntimeException e) {
        log.error("Произошла ошибка статус 400: {}", e.getMessage(), e);
        return new ErrorResponse("Ошибка", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.error("Произошла ошибка 'Не найдено' статус 404: {}", e.getMessage(), e);
        return new ErrorResponse("Ошибка 'Не найдено': ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("Произошла ошибка нарушения целостности данных статус 409: {}", e.getMessage(), e);
        return new ErrorResponse("Уже существует имя категории", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(final ConflictException e) {
        log.error("Произошла конфликтная ошибка статус 409: {}", e.getMessage(), e);
        return new ErrorResponse("Конфликтная ошибка: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException e) {
        log.error("Произошла ошибка недопустимого аргумента: {}", e.getMessage(), e);
        return new ErrorResponse("Ошибка недопустимого аргумента: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestParameterException(
            final MissingServletRequestParameterException e) {
        log.error("Произошла ошибка валидации запроса статус 400: {}", e.getMessage(), e);
        return new ErrorResponse("Ошибка валидации запроса: ", e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        log.error("Произошла внутренняя серверная ошибка стстус 500: {}", e.getMessage(), e);
        return new ErrorResponse("Внутренняя серверная ошибка", e.getMessage());
    }
}
