package ru.practicum.shareit.exception;

public class NotFoundException extends RuntimeException {
    private final String text;

    public NotFoundException(String text) {
        super();
        this.text = text;
    }

    @Override
    public String getMessage() {
        return text;
    }
}