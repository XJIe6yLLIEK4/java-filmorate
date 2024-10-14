package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

@Data
public class User {
    @EqualsAndHashCode.Exclude
    private int id;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String login;
    private String name = "";
    @EqualsAndHashCode.Exclude
    @Past
    private LocalDate birthday;

    public void isValidation() {
        if (name.isBlank()) {
            name = login;
        }
        if (email.isBlank() || !email.contains("@")) {
            throw new ValidationException("email пустой или введен некореткно");
        }
        if (login.isBlank() || login.contains(" ")) {
            throw new ValidationException("login пустой или содержит пробелы");
        }
        if (birthday.isAfter(LocalDate.now())) {
            throw new ValidationException("Неверная дата рождения");
        }
    }
}
