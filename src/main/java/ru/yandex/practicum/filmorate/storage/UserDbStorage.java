package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User postUser(User user) {
        user.isValidation();
        String sql = "INSERT INTO Users (login, email, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());
        log.info("Создан пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public User putUser(User user) {
        user.isValidation();
        String sql = "UPDATE Users SET login = ?, email = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        if (rowsUpdated == 0) {
            throw new UserNotFoundException(user.getId());
        }
        log.info("Обновлен пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public Collection<User> getUsers() {
        String sql = "SELECT * FROM Users";
        Collection<User> users = jdbcTemplate.query(sql, this::mapRowToUser);
        users.forEach(this::loadFriends);
        return users;
    }

    @Override
    public Optional<User> getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        Optional<User> user = jdbcTemplate.query(sql, this::mapRowToUser, id)
                .stream()
                .findFirst();
        user.ifPresent(this::loadFriends);
        return user;
    }

    private void loadFriends(User user) {
        String sql = "SELECT u.* FROM Users u " +
                "JOIN User_friends uf ON u.id = uf.friend_id " +
                "WHERE uf.user_id = ?";
        user.setFriends(new HashSet<>(jdbcTemplate.query(sql, this::mapRowToUser, user.getId())));
    }

    public void addFriend(int userId, int friendId) {
        String sql = "INSERT INTO User_friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, "CONFIRMED");
    }

    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM User_friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setLogin(rs.getString("login"));
        user.setEmail(rs.getString("email"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }
}