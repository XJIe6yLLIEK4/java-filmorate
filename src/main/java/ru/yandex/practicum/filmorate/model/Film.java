package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Film {
    private int id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание фильма не может превышать 200 символов")
    private String description;

    private LocalDate releaseDate;

    private List<Genre> genres = new ArrayList<>();

    private Mpa mpa;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;
    private Set<User> likes = new HashSet<>();
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private final LocalDate minReleaseDate = LocalDate.of(1895, Month.DECEMBER, 28);


    public void isValidation() {
        if (this.getName() == null || this.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (this.getDescription() != null && this.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может превышать 200 символов");
        }
        if (this.getReleaseDate() != null && this.getReleaseDate().isBefore(this.getMinReleaseDate())) {
            throw new ValidationException("Дата релиза не может быть раньше 1895.10.28");
        }
        if (this.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}