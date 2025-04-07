-- noinspection SqlNoDataSourceInspectionForFile

-- noinspection SqlDialectInspectionForFile

CREATE
DATABASE IF NOT EXISTS db;

USE db;
CREATE TABLE IF NOT EXISTS quotation(
    id INT ( 11) NOT NULL AUTO_INCREMENT,
    symbol VARCHAR ( 255 ) NOT NULL,
    volume INT ( 11 ) NOT NULL,
    price DOUBLE NOT NULL,
    date DATETIME,
    PRIMARY KEY ( `id` ) );