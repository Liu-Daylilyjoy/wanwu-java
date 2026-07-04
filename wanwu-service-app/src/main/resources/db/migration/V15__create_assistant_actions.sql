CREATE TABLE IF NOT EXISTS assistant_actions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  assistant_id VARCHAR(64) NOT NULL,
  action_id VARCHAR(96) NOT NULL,
  name VARCHAR(256) NOT NULL DEFAULT '',
  payload LONGTEXT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_assistant_action (user_id, org_id, action_id),
  KEY idx_assistant_actions_assistant (user_id, org_id, assistant_id),
  KEY idx_assistant_actions_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
