CREATE TABLE car
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    type         VARCHAR(250) NOT NULL,
    country      VARCHAR(250) NOT NULL,
    manufacturer VARCHAR(250) DEFAULT NULL,
    create_date  TIMESTAMP    DEFAULT NULL,
    model        VARCHAR(250) DEFAULT NULL
);
