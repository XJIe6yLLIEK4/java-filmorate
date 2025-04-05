package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;
    private final UserDbStorage userDbStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreDbStorage genreDbStorage, MpaDbStorage mpaDbStorage, UserDbStorage userDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDbStorage = genreDbStorage;
        this.mpaDbStorage = mpaDbStorage;
        this.userDbStorage = userDbStorage;
    }

    @Override
    public Film postFilm(Film film) {
        film.isValidation();

        if (film.getMpa() != null) {
            int mpaId = film.getMpa().getId();
            String checkMpaSql = "SELECT COUNT(*) FROM MPA WHERE id = ?";
            Integer mpaCount = jdbcTemplate.queryForObject(checkMpaSql, Integer.class, mpaId);
            if (mpaCount == 0) {
                throw new MpaNotFoundException(mpaId);
            }
        } else {
            throw new ValidationException("MPA не может быть null");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {

            Set<Genre> uniqueGenres = new HashSet<>(film.getGenres());
            film.setGenres(new ArrayList<>(uniqueGenres));

            List<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .toList();

            String checkGenresSql = "SELECT id FROM Genres WHERE id IN (:genreIds)";
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("genreIds", genreIds);

            NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            List<Integer> existingGenreIds = namedParameterJdbcTemplate.queryForList(checkGenresSql, parameters, Integer.class);

            if (existingGenreIds.size() != genreIds.size()) {
                Set<Integer> missingGenres = new HashSet<>(genreIds);
                existingGenreIds.forEach(missingGenres::remove);
                throw new GenreNotFoundException(missingGenres.toString());
            }
        }

        String sql = "INSERT INTO Films (name, description, releaseDate, mpa_id, duration) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            stmt.setObject(4, film.getMpa() != null ? film.getMpa().getId() : null);
            stmt.setInt(5, film.getDuration());
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            }
        }

        log.info("Создан фильм с id: {}", film.getId());
        return film;
    }

    @Override
    public Film putFilm(Film film) {
        film.isValidation();

        String sql = "UPDATE Films SET name = ?, description = ?, releaseDate = ?, mpa_id = ?, duration = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getDuration(),
                film.getId());
        if (rowsUpdated == 0) {
            throw new FilmNotFoundException(film.getId());
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            }
        }

        log.info("Обновлен фильм с id: {}", film.getId());
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        String sql = "SELECT * FROM Films";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        films.forEach(this::loadGenresAndLikes);
        return films.stream()
                .sorted(Comparator.comparing(Film::getId))  // Сортировка по ID
                .toList();
    }

    @Override
    public Optional<Film> getFilmById(int filmId) {
        String sql = "SELECT * FROM Films WHERE id = ?";
        Optional<Film> film = jdbcTemplate.query(sql, this::mapRowToFilm, filmId)
                .stream()
                .findFirst();
        film.ifPresent(this::loadGenresAndLikes);
        return film;
    }

    @Override
    public List<Film> getTopFilms(int count) {
        String sql = "SELECT f.*, COUNT(fl.like_user_id) as like_count " +
                "FROM Films f " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id " +
                "ORDER BY like_count DESC, f.id ASC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);
        films.forEach(this::loadGenresAndLikes);
        return films;
    }

    private void loadGenresAndLikes(Film film) {
        film.setGenres(genreDbStorage.getGenresByFilmId(film.getId()));

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
        film.setDuration(rs.getInt("duration"));

        int mpaId = rs.getInt("mpa_id");
        if (!rs.wasNull()) {
            film.setMpa(mpaDbStorage.getMpaById(mpaId).orElse(null));
        }

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