package ru.practicum;

public class ErrorRequestException extends RuntimeException{
    public ErrorRequestException(String message) {
        super(message);
    }
}
