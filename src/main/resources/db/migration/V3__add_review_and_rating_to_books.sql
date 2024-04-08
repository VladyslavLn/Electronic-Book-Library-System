CREATE TABLE IF NOT EXISTS book_reviews
(
    id             SERIAL PRIMARY KEY,
    book_id        INT REFERENCES books (id),
    user_id        INT REFERENCES users (id),
    review_content TEXT
);

ALTER TABLE book_reviews
    ADD CONSTRAINT user_review_for_each_book UNIQUE (user_id, book_id);

CREATE TABLE IF NOT EXISTS book_ratings
(
    id           SERIAL PRIMARY KEY,
    book_id      INT REFERENCES books (id),
    user_id      INT REFERENCES users (id),
    rating_value DECIMAL(2, 1) CHECK (rating_value >= 0 AND rating_value <= 5)
);

ALTER TABLE book_ratings
    ADD CONSTRAINT user_rating_for_each_book UNIQUE (user_id, book_id);

ALTER TABLE books
    ADD COLUMN IF NOT EXISTS avg_rating DECIMAL(2, 1);
