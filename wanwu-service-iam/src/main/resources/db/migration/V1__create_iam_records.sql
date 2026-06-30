CREATE TABLE IF NOT EXISTS iam_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    record_type VARCHAR(64) NOT NULL,
    record_id VARCHAR(128) NOT NULL,
    payload LONGTEXT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_iam_records_type_id (record_type, record_id),
    KEY idx_iam_records_type (record_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
