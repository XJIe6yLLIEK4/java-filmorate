package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;

@Data
public class Film {
    @EqualsAndHashCode.Exclude
    private int id;
    @NotBlank
    private String name;
    @EqualsAndHashCode.Exclude
    private String description;
    private LocalDate releaseDate;
    @PositiveOrZero
    private int duration;

    public void isValidation() {
        if (name.isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (description.length() > 200) {
            throw new ValidationException("Описание не может быть больше 200 символов");
        }
        if (releaseDate.isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            throw new ValidationException("Неверная дата релиза");
        }
        if (duration < 0) {
            throw new ValidationException("Продолжительность фильма не иожет быть отрицательной");
        }
    }
}
