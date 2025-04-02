package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film postFilm(Film film) {
        film.isValidation();
        String sql = "INSERT INTO Films (name, description, releaseDate, genre, rating, duration) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            stmt.setString(4, film.getGenre());
            stmt.setString(5, film.getRating());
            stmt.setInt(6, film.getDuration());
            return stmt;
        }, keyHolder);
        film.setId(keyHolder.getKey().intValue());
        log.info("Создан фильм с id: {}", film.getId());
        return film;
    }

    @Override
    public Film putFilm(Film film) {
        film.isValidation();
        String sql = "UPDATE Films SET name = ?, description = ?, releaseDate = ?, genre = ?, rating = ?, duration = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getGenre(),
                film.getRating(),
                film.getDuration(),
                film.getId());
        if (rowsUpdated == 0) {
            throw new FilmNotFoundException(film.getId());
        }
        log.info("Обновлен фильм с id: {}", film.getId());
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        String sql = "SELECT * FROM Films";
        Collection<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        films.forEach(this::loadLikes);
        return films;
    }

    @Override
    public Optional<Film> getFilmById(int filmId) {
        String sql = "SELECT * FROM Films WHERE id = ?";
        Optional<Film> film = jdbcTemplate.query(sql, this::mapRowToFilm, filmId)
                .stream()
                .findFirst();
        film.ifPresent(this::loadLikes);
        return film;
    }

    private void loadLikes(Film film) {
        String sql = "SELECT u.* FROM Users u " +
                "JOIN film_likes fl ON u.id = fl.like_user_id " +
                "WHERE fl.film_id = ?";
        film.setLikes(new HashSet<>(jdbcTemplate.query(sql, this::mapRowToUser, film.getId())));
    }

    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, like_user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND like_user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        LocalDate releaseDate = rs.getDate("releaseDate") != null ? rs.getDate("releaseDate").toLocalDate() : null;
        film.setReleaseDate(releaseDate);
        film.setGenre(rs.getString("genre"));
        film.setRating(rs.getString("rating"));
        film.setDuration(rs.getInt("duration"));
        return film;
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