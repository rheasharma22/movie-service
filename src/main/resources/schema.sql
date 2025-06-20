CREATE TABLE users (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      name VARCHAR(100),
                      email VARCHAR(100)
);

CREATE TABLE movie (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(100),
                       genre VARCHAR(100),
                       rating DOUBLE,
                       user_id BIGINT,
                       CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);
