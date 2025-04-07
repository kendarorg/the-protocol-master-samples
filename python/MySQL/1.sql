-- noinspection SqlNoDataSourceInspectionForFile

-- noinspection SqlDialectInspectionForFile

CREATE
DATABASE IF NOT EXISTS db;

USE db;

CREATE TABLE IF NOT EXISTS `quotation` (
     `id` int NOT NULL AUTO_INCREMENT,
     `symbol` varchar(5) NOT NULL,
     `date` datetime NOT NULL,
     `price` double NOT NULL,
     `volume` int NOT NULL,
     PRIMARY KEY (`id`)
);

