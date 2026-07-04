CREATE TABLE IF NOT EXISTS general_agent_configs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  config_json LONGTEXT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_general_agent_config_scope (user_id, org_id),
  KEY idx_general_agent_configs_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
