ALTER TABLE assistant_draft_configs
  ADD COLUMN workflow_infos JSON NULL AFTER recommend_questions,
  ADD COLUMN mcp_infos JSON NULL AFTER workflow_infos,
  ADD COLUMN tool_infos JSON NULL AFTER mcp_infos,
  ADD COLUMN skill_infos JSON NULL AFTER tool_infos,
  ADD COLUMN multi_agent_infos JSON NULL AFTER skill_infos;
