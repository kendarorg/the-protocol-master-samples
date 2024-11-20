
-- noinspection SqlNoDataSourceInspectionForFile

-- noinspection SqlDialectInspectionForFile

CREATE TABLE task (
    id INT NOT NULL AUTO_INCREMENT,
    task_name VARCHAR(255) NOT NULL,
    priority VARCHAR(255) NOT NULL,
    status VARCHAR(255),
    notes TEXT,
    archive_date DATETIME,
);