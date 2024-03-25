package ru.practicum.shareit.exception;

public class ValidationException extends RuntimeException {
    private final String text;

    public ValidationException(String text) {
        super();
        this.text = text;
    }

    @Override
    public String getMessage() {
        return text;
    }
}
