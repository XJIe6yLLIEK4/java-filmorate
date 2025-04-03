package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User postUser(User user);

    User putUser(User user);

    Collection<User> getUsers();

    Optional<User> getUserById(int id);
}
