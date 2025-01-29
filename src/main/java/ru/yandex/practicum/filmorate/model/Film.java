package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private int id;
    @NotBlank
    private String name;
    @EqualsAndHashCode.Exclude
    private String description;
    private LocalDate releaseDate;
    @PositiveOrZero
    private int duration;
    @ToString.Exclude @EqualsAndHashCode.Exclude @JsonIgnore
    private Set<User> likes = new HashSet<>();
    @ToString.Exclude @EqualsAndHashCode.Exclude @JsonIgnore
    private final int maxDuration = 200;
    @ToString.Exclude @EqualsAndHashCode.Exclude @JsonIgnore
    private final LocalDate minReleaseDate = LocalDate.of(1895, Month.DECEMBER, 28);

    public void isValidation() {
        if (name.isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (description == null) {
            throw new ValidationException("Описание не может быть пустым");
        } else if (description.length() > maxDuration) {
            throw new ValidationException("Описание не может быть больше 200 символов");
        }
        if (releaseDate == null) {
            throw new ValidationException("Дата релиза не может быть пустой");
        } else if (releaseDate.isBefore(minReleaseDate)) {
            throw new ValidationException("Неверная дата релиза");
        }
        if (duration < 0) {
            throw new ValidationException("Продолжительность фильма не может быть отрицательной");
        }
    }
}
