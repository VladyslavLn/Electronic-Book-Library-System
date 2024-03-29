CREATE TABLE IF NOT EXISTS books
(
    id       SERIAL PRIMARY KEY,
    name     TEXT NOT NULL,
    author   TEXT,
    language TEXT,
    file_key TEXT
);
