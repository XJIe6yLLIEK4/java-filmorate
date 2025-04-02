package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ContentNotException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmDbStorage filmDbStorage; // Для прямого доступа к методам addLike/removeLike

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmDbStorage") FilmDbStorage filmDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.filmDbStorage = filmDbStorage;
    }

    public Film addLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new FilmNotFoundException(filmId));
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (film.getLikes().contains(user)) {
            throw new ValidationException("Пользователь " + user.getId() + " уже оценил этот фильм");
        }

        filmDbStorage.addLike(filmId, userId);
        film.getLikes().add(user);
        return film;
    }

    public Film deleteLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new FilmNotFoundException(filmId));
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!film.getLikes().contains(user)) {
            throw new ContentNotException("Пользователь " + user.getId() + " еще не оценил этот фильм");
        }

        filmDbStorage.removeLike(filmId, userId);
        film.getLikes().remove(user);
        return film;
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.getFilms().stream()
                .sorted((film1, film2) -> {
                    int likes1 = film1.getLikes() != null ? film1.getLikes().size() : 0;
                    int likes2 = film2.getLikes() != null ? film2.getLikes().size() : 0;
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }
}