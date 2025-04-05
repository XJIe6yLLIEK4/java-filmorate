package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    User postUser(User user);

    User putUser(User user);

    Collection<User> getUsers();

    Optional<User> getUserById(int id);

    Set<User> getCommonFriends(int userId1, int userId2);
}
