CREATE TABLE IF NOT EXISTS workflow_run_records (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  finished_at BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  org_id VARCHAR(64) NOT NULL,
  workflow_id VARCHAR(64) NOT NULL,
  run_id VARCHAR(96) NOT NULL,
  status VARCHAR(32) NOT NULL,
  input_json JSON NULL,
  output_json JSON NULL,
  cost_millis BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_workflow_run_id (user_id, org_id, run_id),
  KEY idx_workflow_run_workflow_id (workflow_id),
  KEY idx_workflow_run_user_org_workflow (user_id, org_id, workflow_id),
  KEY idx_workflow_run_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
