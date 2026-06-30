CREATE TABLE IF NOT EXISTS open_api_keys (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  `key` VARCHAR(128) NOT NULL,
  description TEXT NULL,
  name VARCHAR(128) NOT NULL,
  status TINYINT(1) NOT NULL DEFAULT 1,
  expired_at BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_open_api_key_key (`key`),
  UNIQUE KEY uk_open_api_key_name (user_id, org_id, name),
  KEY idx_open_api_key_user_org (user_id, org_id),
  KEY idx_open_api_key_status (status),
  KEY idx_open_api_key_expired_at (expired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE IF NOT EXISTS api_keys (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  app_type VARCHAR(32) NOT NULL,
  api_key VARCHAR(128) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_api_key_api_key (api_key),
  KEY idx_api_key_user_org (user_id, org_id),
  KEY idx_api_key_app (user_id, org_id, app_id, app_type),
  KEY idx_api_key_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
