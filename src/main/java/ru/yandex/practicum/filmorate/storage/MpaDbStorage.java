package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<Mpa> getAllMpa() {
        String sql = "SELECT * FROM MPA";
        List<Mpa> mpaList = jdbcTemplate.query(sql, this::mapRowToMpa);
        return mpaList.stream()
                .sorted(Comparator.comparing(Mpa::getId))
                .toList();
    }

    public Optional<Mpa> getMpaById(int id) {
        String sql = "SELECT * FROM MPA WHERE id = ?";
        return jdbcTemplate.query(sql, this::mapRowToMpa, id)
                .stream()
                .findFirst();
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    }
}