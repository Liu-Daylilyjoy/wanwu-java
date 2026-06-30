CREATE TABLE IF NOT EXISTS assistant_snapshots (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  assistant_id VARCHAR(64) NOT NULL,
  version VARCHAR(64) NOT NULL,
  snapshot_desc VARCHAR(512) NOT NULL DEFAULT '',
  category INT NOT NULL,
  assistant_info_json JSON NULL,
  assistant_config_json JSON NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_assistant_snapshot_version (user_id, org_id, assistant_id, version),
  KEY idx_assistant_snapshot_assistant_id (assistant_id),
  KEY idx_assistant_snapshot_user_id (user_id),
  KEY idx_assistant_snapshot_org_id (org_id),
  KEY idx_assistant_snapshot_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
