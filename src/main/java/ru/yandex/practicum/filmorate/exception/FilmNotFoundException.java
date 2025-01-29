package ru.yandex.practicum.filmorate.exception;

public class FilmNotFoundException extends RuntimeException {

    public FilmNotFoundException(int filmId) {
        super("Фильм с id: " + filmId + " не найден");
    }

    public FilmNotFoundException(String m) {
        super(m);
    }
}
