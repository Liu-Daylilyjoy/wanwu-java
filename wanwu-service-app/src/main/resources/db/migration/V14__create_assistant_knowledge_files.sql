CREATE TABLE IF NOT EXISTS assistant_knowledge_files (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  assistant_id VARCHAR(64) NOT NULL,
  file_id VARCHAR(96) NOT NULL,
  file_name VARCHAR(512) NOT NULL DEFAULT '',
  file_size BIGINT NOT NULL DEFAULT 0,
  content_type VARCHAR(128) NOT NULL DEFAULT '',
  status VARCHAR(32) NOT NULL DEFAULT 'success',
  url VARCHAR(1024) NOT NULL DEFAULT '',
  PRIMARY KEY (id),
  UNIQUE KEY uk_assistant_knowledge_file (user_id, org_id, file_id),
  KEY idx_assistant_knowledge_files_assistant (user_id, org_id, assistant_id),
  KEY idx_assistant_knowledge_files_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
