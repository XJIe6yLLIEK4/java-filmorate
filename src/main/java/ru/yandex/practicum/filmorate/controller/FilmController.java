package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final HashMap<Integer, Film> films = new HashMap<>();
    private int count = 1;

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        try {
            film.isValidation();

            if (films.containsKey(film.getId())) {
                log.warn("Фильм с таким ID уже существует: {}", film.getId());
                return new ResponseEntity<>(film, HttpStatus.BAD_REQUEST);
            }

            film.setId(count);
            films.put(count, film);
            count++;
            return new ResponseEntity<>(film, HttpStatus.CREATED);
        } catch (ValidationException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            return new ResponseEntity<>(film, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("Вызов getFilms");
        return new ArrayList<>(films.values());
    }

    @PutMapping
    public ResponseEntity<Film> updateFilms(@Valid @RequestBody Film film) {
        try {
            film.isValidation();

            if (films.containsKey(film.getId())) {
                films.put(film.getId(), film);
                return new ResponseEntity<>(film, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(film, HttpStatus.NOT_FOUND);
            }
        } catch (ValidationException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            return new ResponseEntity<>(film, HttpStatus.BAD_REQUEST);
        }
    }
}
