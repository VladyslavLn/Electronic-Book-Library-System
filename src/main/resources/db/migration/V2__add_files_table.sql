CREATE TABLE IF NOT EXISTS files
(
    file_key       TEXT NOT NULL UNIQUE,
    upload_user_id INT,
    FOREIGN KEY (upload_user_id) REFERENCES users (id),
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
