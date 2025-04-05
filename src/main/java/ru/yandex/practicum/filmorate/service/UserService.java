package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ContentNotException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Set;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final UserDbStorage userDbStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("userDbStorage") UserDbStorage userDbStorage) {
        this.userStorage = userStorage;
        this.userDbStorage = userDbStorage;
    }

    @Transactional
    public User addFriends(int userId1, int userId2) {
        User user1 = userStorage.getUserById(userId1)
                .orElseThrow(() -> new UserNotFoundException(userId1));
        User user2 = userStorage.getUserById(userId2)
                .orElseThrow(() -> new UserNotFoundException(userId2));

        if (user1.getFriends().contains(user2)) {
            throw new ValidationException("Пользователь " + user2.getId() + " уже добавлен в друзья");
        }

        userDbStorage.addFriend(userId1, userId2);
        user1.getFriends().add(user2);
        return user1;
    }

    @Transactional
    public User deleteFriends(int userId1, int userId2) {
        User user1 = userStorage.getUserById(userId1)
                .orElseThrow(() -> new UserNotFoundException(userId1));
        User user2 = userStorage.getUserById(userId2)
                .orElseThrow(() -> new UserNotFoundException(userId2));

        if (!user1.getFriends().contains(user2)) {
            throw new ContentNotException("Пользователь " + user2.getId() + " не найден в списке друзей");
        }

        userDbStorage.removeFriend(userId1, userId2);
        user1.getFriends().remove(user2);
        return user1;
    }

    public Set<User> getCommonFriends(int userId1, int userId2) {
        userStorage.getUserById(userId1)
                .orElseThrow(() -> new UserNotFoundException(userId1));
        userStorage.getUserById(userId2)
                .orElseThrow(() -> new UserNotFoundException(userId2));
        return userDbStorage.getCommonFriends(userId1, userId2);
    }
}