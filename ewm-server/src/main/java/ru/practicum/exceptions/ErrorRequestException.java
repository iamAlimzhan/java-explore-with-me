package ru.practicum.exceptions;

public class ErrorRequestException extends RuntimeException {
    public ErrorRequestException(String message) {
        super(message);
    }
}
