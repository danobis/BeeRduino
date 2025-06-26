-- schema.sql
USE data_schema;

GRANT ALL PRIVILEGES ON data_schema.* TO 'mariadb'@'%';
FLUSH PRIVILEGES;

DROP TABLE IF EXISTS data_schema.beehive_registry;
CREATE TABLE data_schema.beehive_registry
(
    beehive_uuid BINARY(16)  NOT NULL PRIMARY KEY,
    timestamp    DATETIME(6) NOT NULL,
    status       enum (
        'ACTIVE',
        'ORPHANED')          NOT NULL,
    deleted      bit         NOT NULL
);

DROP TABLE IF EXISTS data_schema.measurements;
CREATE TABLE data_schema.measurements
(
    uuid         BINARY(16)  NOT NULL PRIMARY KEY,
    timestamp    DATETIME(6) NOT NULL,
    value        DOUBLE      NOT NULL,
    type         enum (
        'DEFAULT',
        'HUMIDITY_INSIDE',
        'HUMIDITY_OUTSIDE',
        'TEMPERATURE_INSIDE',
        'TEMPERATURE_OUTSIDE',
        'WEIGHT')            NOT NULL,
    unit         enum (
        'CELSIUS',
        'DEFAULT',
        'GRAM',
        'PERCENTAGE')        NOT NULL,
    beehive_uuid BINARY(16)  NOT NULL,
    deleted      bit         NOT NULL
);

SHOW TABLES;
