-- schema.sql
USE master_schema;

GRANT ALL PRIVILEGES ON master_schema.* TO 'mariadb'@'%';
FLUSH PRIVILEGES;

CREATE TABLE master_schema.locations
(
    uuid      BINARY(16)   NOT NULL PRIMARY KEY,
    timestamp DATETIME(6)  NOT NULL,
    latitude  DOUBLE       NOT NULL,
    longitude DOUBLE       NOT NULL,
    comment   VARCHAR(300) NOT NULL
);

CREATE TABLE master_schema.owners
(
    uuid         BINARY(16)   NOT NULL PRIMARY KEY,
    timestamp    DATETIME(6)  NOT NULL,
    phone_number VARCHAR(25)  NOT NULL,
    email        VARCHAR(255) NOT NULL,
    description  VARCHAR(300) NOT NULL
);

CREATE TABLE master_schema.beehives
(
    uuid          BINARY(16)   NOT NULL PRIMARY KEY,
    timestamp     DATETIME(6)  NOT NULL,
    comment       VARCHAR(300) NOT NULL,
    location_uuid BINARY(16)   NOT NULL,
    owner_uuid    BINARY(16)   NOT NULL
);

ALTER TABLE master_schema.beehives
    ADD CONSTRAINT fk_beehives_locations
        FOREIGN KEY (location_uuid) REFERENCES master_schema.locations (uuid);

ALTER TABLE master_schema.beehives
    ADD CONSTRAINT fk_beehives_owners
        FOREIGN KEY (owner_uuid) REFERENCES master_schema.owners (uuid);

SHOW TABLES;
