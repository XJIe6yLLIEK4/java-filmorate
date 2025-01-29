package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ContentNotException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriends(int userId1, int userId2) {
        User user1 = userStorage.getUserById(userId1)
                .orElseThrow(() -> new UserNotFoundException(userId1));
        User user2 = userStorage.getUserById(userId2)
                .orElseThrow(() -> new UserNotFoundException(userId2));

        if (!user1.getFriends().add(user2)) {
            throw new ValidationException("Пользователь " + user2.getId() + " уже добавлен в друзья");
        }
        if (!user2.getFriends().add(user1)) {
            throw new ValidationException("Пользователь " + user1.getId() + " уже добавлен в друзья");
        }
        return user1;
    }

    public User deleteFriends(int userId1, int userId2) {
        User user1 = userStorage.getUserById(userId1)
                .orElseThrow(() -> new UserNotFoundException(userId1));
        User user2 = userStorage.getUserById(userId2)
                .orElseThrow(() -> new UserNotFoundException(userId2));

        if (!user1.getFriends().remove(user2)) {
            throw new ContentNotException("Пользователь " + user2.getId() + " не найден в списке друзей");
        }
        if (!user2.getFriends().remove(user1)) {
            throw new ContentNotException("Пользователь " + user1.getId() + " не найден в списке друзей");
        }
        return user1;
    }

    public Set<User> getCommonFriends(int userId1, int userId2) {
        User user1 = userStorage.getUserById(userId1)
                .orElseThrow(() -> new UserNotFoundException(userId1));
        User user2 = userStorage.getUserById(userId2)
                .orElseThrow(() -> new UserNotFoundException(userId2));
        return user1.getFriends().stream()
                .filter(friend -> user2.getFriends().contains(friend))
                .collect(Collectors.toSet());
    }
}
