CREATE TABLE IF NOT EXISTS general_agent_conversations (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  thread_id VARCHAR(96) NOT NULL,
  title VARCHAR(512) NOT NULL DEFAULT '',
  skill_conversation TINYINT(1) NOT NULL DEFAULT 0,
  skill_id VARCHAR(96) NOT NULL DEFAULT '',
  preview_id VARCHAR(96) NOT NULL DEFAULT '',
  model_config_json LONGTEXT NOT NULL,
  runs_json LONGTEXT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_general_agent_conversation_thread (user_id, org_id, thread_id),
  KEY idx_general_agent_conversations_scope (user_id, org_id, updated_at),
  KEY idx_general_agent_conversations_preview (user_id, org_id, preview_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
