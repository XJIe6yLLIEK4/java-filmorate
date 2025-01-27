package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Film putFilm(Film film);

    Film postFilm(Film film);

    Collection<Film> getFilms();

    Optional<Film> getFilmById(int id);
}
