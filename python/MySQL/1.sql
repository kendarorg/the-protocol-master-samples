-- noinspection SqlNoDataSourceInspectionForFile

-- noinspection SqlDialectInspectionForFile

CREATE
DATABASE IF NOT EXISTS db;

USE
db;

CREATE TABLE IF NOT EXISTS  `chatmessage` (
                               `id` int NOT NULL AUTO_INCREMENT,
                               `creator` varchar(255) NOT NULL,
                               `destination` varchar(255) NOT NULL,
                               `content` varchar(255) NOT NULL,
                               `read` tinyint(1) NOT NULL,
                               `timestamp` datetime NOT NULL,
                               PRIMARY KEY (`id`)
);