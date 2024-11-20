
-- noinspection SqlNoDataSourceInspectionForFile

-- noinspection SqlDialectInspectionForFile

CREATE TABLE IF NOT EXISTS task  (
    id INT(11)  NOT NULL AUTO_INCREMENT,
    task_name VARCHAR(255) NOT NULL,
    priority VARCHAR(255) NOT NULL,
    status VARCHAR(255),
    notes TEXT,
    archive_date DATETIME,
    PRIMARY KEY (`id`)
);