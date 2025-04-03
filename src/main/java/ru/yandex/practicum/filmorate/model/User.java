package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;
    @NotBlank @Email
    private String email;
    @NotBlank
    private String login;
    private String name;
    @Past
    private LocalDate birthday;
    @ToString.Exclude @EqualsAndHashCode.Exclude @JsonIgnore
    private Set<User> friends = new HashSet<>();

    public void isValidation() {
        if (name == null || name.isBlank()) {
            name = login;
        }
        if (email.isBlank() || !email.contains("@")) {
            throw new ValidationException("email пустой или введен некорректно");
        }
        if (login.isBlank() || login.contains(" ")) {
            throw new ValidationException("login пустой или содержит пробелы");
        }
        if (birthday == null) {
            throw new ValidationException("Не указана дата рождения");
        } else if (birthday.isAfter(LocalDate.now())) {
            throw new ValidationException("Неверная дата рождения");
        }
    }
}
