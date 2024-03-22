CREATE TABLE IF NOT EXISTS books
(
    id       SERIAL PRIMARY KEY,
    name     TEXT NOT NULL,
    author   TEXT NOT NULL,
    language TEXT,
    file_key TEXT NOT NULL,
    FOREIGN KEY (file_key) REFERENCES files (file_key)
);
