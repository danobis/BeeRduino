-- schema.sql
USE beehive_schema;

GRANT ALL PRIVILEGES ON beehive_schema.* TO 'default'@'%';
FLUSH PRIVILEGES;

CREATE TABLE beehive_schema.locations
(
    uuid      BINARY(16)   NOT NULL PRIMARY KEY,
    timestamp DATETIME(6)  NOT NULL,
    latitude  DOUBLE       NOT NULL,
    longitude DOUBLE       NOT NULL,
    comment   VARCHAR(300) NOT NULL
);

CREATE TABLE beehive_schema.owners
(
    uuid         BINARY(16)   NOT NULL PRIMARY KEY,
    timestamp    DATETIME(6)  NOT NULL,
    phone_number VARCHAR(25)  NOT NULL,
    email        VARCHAR(255) NOT NULL,
    description  VARCHAR(300) NOT NULL
);

CREATE TABLE beehive_schema.beehives
(
    uuid          BINARY(16)   NOT NULL PRIMARY KEY,
    timestamp     DATETIME(6)  NOT NULL,
    comment       VARCHAR(300) NOT NULL,
    location_uuid BINARY(16)   NOT NULL,
    owner_uuid    BINARY(16)   NOT NULL
);

ALTER TABLE beehive_schema.beehives
    ADD CONSTRAINT fk_beehives_locations
        FOREIGN KEY (location_uuid) REFERENCES beehive_schema.locations (uuid);

ALTER TABLE beehive_schema.beehives
    ADD CONSTRAINT fk_beehives_owners
        FOREIGN KEY (owner_uuid) REFERENCES beehive_schema.owners (uuid);

CREATE TABLE beehive_schema.measurements
(
    uuid         BINARY(16)  NOT NULL PRIMARY KEY,
    timestamp    DATETIME(6) NOT NULL,
    value        DOUBLE      NOT NULL,
    type         enum (
        'SENSOR_DEFAULT', 
        'SENSOR_HUMIDITY_INSIDE', 
        'SENSOR_HUMIDITY_OUTSIDE', 
        'SENSOR_TEMPERATURE_INSIDE', 
        'SENSOR_TEMPERATURE_OUTSIDE', 
        'SENSOR_WEIGHT')     NOT NULL,
    unit         enum (
        'UNIT_CELSIUS', 
        'UNIT_DEFAULT', 
        'UNIT_GRAM', 
        'UNIT_PERCENTAGE')   NOT NULL,
    beehive_uuid BINARY(16)  NOT NULL,
    owner_uuid   BINARY(16)  NOT NULL
);

ALTER TABLE beehive_schema.measurements
    ADD CONSTRAINT fk_measurements_beehives
    FOREIGN KEY (beehive_uuid) REFERENCES beehive_schema.beehives (uuid);

SHOW TABLES;
