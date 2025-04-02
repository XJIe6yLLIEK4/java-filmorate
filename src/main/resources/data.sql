-- Очистка таблиц перед вставкой (опционально, если нужно перезаписывать данные)
DELETE FROM film_likes;
DELETE FROM User_friends;
DELETE FROM Films;
DELETE FROM Users;

-- Добавление тестовых пользователей
INSERT INTO Users (login, email, name, birthday) VALUES
('user1', 'user1@example.com', 'User One', '1990-05-15'),
('user2', 'user2@example.com', 'User Two', '1995-08-22');

-- Добавление тестовых фильмов
INSERT INTO Films (name, description, releaseDate, genre, rating, duration) VALUES
('Film 1', 'Description of Film 1', '2020-01-01', 'Drama', 'PG-13', 120),
('Film 2', 'Description of Film 2', '2021-06-15', 'Comedy', 'R', 90);

-- Добавление дружеских связей
INSERT INTO User_friends (user_id, friend_id, status)
SELECT 1, 2, 'CONFIRMED'
WHERE NOT EXISTS (
    SELECT 1 FROM User_friends WHERE user_id = 1 AND friend_id = 2
);

-- Добавление лайков
INSERT INTO film_likes (film_id, like_user_id)
SELECT 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM film_likes WHERE film_id = 1 AND like_user_id = 1
);

INSERT INTO film_likes (film_id, like_user_id)
SELECT 1, 2
WHERE NOT EXISTS (
    SELECT 1 FROM film_likes WHERE film_id = 1 AND like_user_id = 2
);