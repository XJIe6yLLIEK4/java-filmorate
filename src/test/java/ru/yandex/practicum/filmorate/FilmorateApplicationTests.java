package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class FilmorateApplicationTests {

    private final UserDbStorage userStorage;

    @Test
    public void testPostUser() {
        User user = new User();
        user.setLogin("testLogin");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.postUser(user);

        assertThat(savedUser.getId()).isGreaterThan(0);
        assertThat(savedUser.getLogin()).isEqualTo("testLogin");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testPutUser() {
        User user = new User();
        user.setLogin("testLogin");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.postUser(user);

        savedUser.setName("Updated Name");
        savedUser.setEmail("updated@example.com");
        User updatedUser = userStorage.putUser(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");

        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
    }

    @Test
    public void testGetUserById() {
        User user = new User();
        user.setLogin("testLogin");
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.postUser(user);

        Optional<User> userOptional = userStorage.getUserById(savedUser.getId());

        assertThat(userOptional).isPresent();
        userOptional.ifPresent(u -> {
            assertThat(u.getId()).isEqualTo(savedUser.getId());
            assertThat(u.getLogin()).isEqualTo("testLogin");
            assertThat(u.getEmail()).isEqualTo("test@example.com");
            assertThat(u.getName()).isEqualTo("Test User");
            assertThat(u.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
        });
    }

    @Test
    public void testAddAndRemoveFriend() {
        // Создаем двух пользователей
        User user1 = new User();
        user1.setLogin("user1");
        user1.setEmail("user1@example.com");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser1 = userStorage.postUser(user1);

        User user2 = new User();
        user2.setLogin("user2");
        user2.setEmail("user2@example.com");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser2 = userStorage.postUser(user2);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());

        Optional<User> user1Optional = userStorage.getUserById(savedUser1.getId());
        assertThat(user1Optional).isPresent();
        user1Optional.ifPresent(u -> {
            assertThat(u.getFriends()).isNotEmpty();
            assertThat(u.getFriends()).anyMatch(friend -> friend.getId() == savedUser2.getId());
        });

        userStorage.removeFriend(savedUser1.getId(), savedUser2.getId());

        Optional<User> user1AfterRemovalOptional = userStorage.getUserById(savedUser1.getId());
        assertThat(user1AfterRemovalOptional).isPresent();
        user1AfterRemovalOptional.ifPresent(u -> {
            assertThat(u.getFriends()).isEmpty();
        });
    }
}