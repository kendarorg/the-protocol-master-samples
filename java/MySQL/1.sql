
-- noinspection SqlNoDataSourceInspectionForFile

-- noinspection SqlDialectInspectionForFile

CREATE TABLE IF NOT EXISTS quotations  (
    id INT(11)  NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(255) NOT NULL,
    volume INT(11) NOT NULL,
    buy DOUBLE NOT NULL,
    sell DOUBLE NOT NULL,
    date DATETIME,
    PRIMARY KEY (`id`)
);