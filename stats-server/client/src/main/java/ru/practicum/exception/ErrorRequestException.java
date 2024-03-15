package ru.practicum.exception;

public class ErrorRequestException extends RuntimeException {
    public ErrorRequestException(String message) {
        super(message);
    }
}
