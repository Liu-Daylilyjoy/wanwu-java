CREATE TABLE IF NOT EXISTS apps (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  app_type VARCHAR(32) NOT NULL,
  publish_type VARCHAR(32) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_app_created_at (created_at),
  KEY idx_app_user_id (user_id),
  KEY idx_app_org_id (org_id),
  KEY idx_app_app_id (app_id),
  KEY idx_app_app_type (app_type),
  KEY idx_app_publish_type (publish_type),
  UNIQUE KEY uk_app_user_org_type_id (user_id, org_id, app_type, app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE IF NOT EXISTS assistant_drafts (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  assistant_id VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(512) NOT NULL,
  avatar_key VARCHAR(512) NOT NULL,
  avatar_path VARCHAR(512) NOT NULL,
  category INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_assistant_draft_assistant_id (assistant_id),
  KEY idx_assistant_draft_user_id (user_id),
  KEY idx_assistant_draft_org_id (org_id),
  KEY idx_assistant_draft_name (name),
  KEY idx_assistant_draft_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
