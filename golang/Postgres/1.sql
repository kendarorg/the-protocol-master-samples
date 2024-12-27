-- noinspection SqlNoDataSourceInspectionForFile

-- noinspection SqlDialectInspectionForFile



CREATE TABLE messages (
    id serial PRIMARY KEY,
    channel VARCHAR(255),
    sender VARCHAR(255),
    message TEXT
);