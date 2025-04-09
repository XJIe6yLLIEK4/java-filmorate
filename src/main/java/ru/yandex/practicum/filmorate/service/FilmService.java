package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ContentNotException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmDbStorage filmDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmDbStorage") FilmDbStorage filmDbStorage,
                       GenreDbStorage genreDbStorage,
                       MpaDbStorage mpaDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.filmDbStorage = filmDbStorage;
        this.genreDbStorage = genreDbStorage;
        this.mpaDbStorage = mpaDbStorage;
    }

    public Film create(Film film) {
        if (film.getMpa() != null) {
            mpaDbStorage.getMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new MpaNotFoundException(film.getMpa().getId()));
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> uniqueGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            List<Genre> uniqueGenres = uniqueGenreIds.stream()
                    .map(id -> genreDbStorage.getGenreById(id)
                            .orElseThrow(() -> new GenreNotFoundException(id)))
                    .sorted(Comparator.comparing(Genre::getId))
                    .toList();
            film.setGenres(uniqueGenres);
        }

        return filmStorage.postFilm(film);
    }

    public Film update(Film film) {
        filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new FilmNotFoundException(film.getId()));

        if (film.getMpa() != null) {
            mpaDbStorage.getMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new MpaNotFoundException(film.getMpa().getId()));
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> uniqueGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            List<Genre> uniqueGenres = uniqueGenreIds.stream()
                    .map(id -> genreDbStorage.getGenreById(id)
                            .orElseThrow(() -> new GenreNotFoundException(id)))
                    .sorted(Comparator.comparing(Genre::getId))
                    .toList();
            film.setGenres(uniqueGenres);
        }

        return filmStorage.putFilm(film);
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
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным");
        }
        return filmDbStorage.getTopFilms(count);
    }
}