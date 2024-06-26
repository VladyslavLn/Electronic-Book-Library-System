CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users
(
    id         SERIAL PRIMARY KEY,
    email      TEXT NOT NULL UNIQUE,
    first_name TEXT NOT NULL,
    last_name  TEXT NOT NULL,
    password   TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    changed_at TIMESTAMP WITH TIME ZONE
);

INSERT INTO roles(name)
VALUES ('ADMIN'),
       ('USER');

CREATE TABLE users_roles
(
    role_id INT,
    FOREIGN KEY (role_id) REFERENCES roles (id),
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
