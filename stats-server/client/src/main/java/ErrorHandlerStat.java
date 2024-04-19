import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ErrorResponse;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ErrorHandlerStat {

    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Произошла ошибка валидации со статусом 400: {}", e.getMessage(), e);
        return ErrorResponse.builder()
                .message("Ошибка валидации")
                .reason(e.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("Произошла ошибка валидации: Не указан обязательный параметр запроса со статусом 400", e);
        return ErrorResponse.builder()
                .message("Ошибка валидации: Не указан обязательный параметр запроса")
                .reason(e.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }

    @ExceptionHandler({Throwable.class, RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(Throwable e) {
        log.error("Произошла внутренняя ошибка сервера со статусом 500: {}", e.getMessage(), e);
        return ErrorResponse.builder()
                .message("Внутренняя ошибка сервера")
                .reason(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }
}
