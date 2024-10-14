package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int count = 1;

    @PostMapping
    public ResponseEntity<User> addUser (@Valid @RequestBody User user) {
        try {
            user.isValidation();

            if (users.containsKey(user.getId())) {
                log.warn("Пользователь с таким ID уже существует: {}", user.getId());
                return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
            }
            user.setId(count);
            users.put(count, user);
            count++;
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (ValidationException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public List<User> getUsers () {
        return new ArrayList<>(users.values());
    }

    @PutMapping
    public ResponseEntity<User> updateUser (@Valid @RequestBody User user) {
        try {
            user.isValidation();

            if (users.containsKey(user.getId())) {
                users.put(user.getId(), user);
                return new ResponseEntity<>(user, HttpStatus.OK);
            } else {
                log.warn("Пользователь с id: {}", user.getId());
                return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
            }
        } catch (ValidationException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
        }
    }
}
