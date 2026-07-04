CREATE TABLE IF NOT EXISTS app_favorites (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  app_type VARCHAR(32) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_favorite_user_type_id (user_id, app_type, app_id),
  KEY idx_app_favorite_created_at (created_at),
  KEY idx_app_favorite_user_id (user_id),
  KEY idx_app_favorite_app_id (app_id),
  KEY idx_app_favorite_app_type (app_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE IF NOT EXISTS app_histories (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  app_type VARCHAR(32) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_history_user_type_id (user_id, app_type, app_id),
  KEY idx_app_history_created_at (created_at),
  KEY idx_app_history_updated_at (updated_at),
  KEY idx_app_history_user_id (user_id),
  KEY idx_app_history_app_id (app_id),
  KEY idx_app_history_app_type (app_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
