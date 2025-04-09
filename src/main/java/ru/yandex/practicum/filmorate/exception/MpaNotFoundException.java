package ru.yandex.practicum.filmorate.exception;

public class MpaNotFoundException extends RuntimeException {
    public MpaNotFoundException(int id) {
        super("MPA с id " + id + " не найден");
    }
}