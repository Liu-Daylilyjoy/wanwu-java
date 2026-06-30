package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantDraftConfigEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssistantDraftConfigMapper extends BaseMapper<AssistantDraftConfigEntity> {

    @Insert({
            "INSERT INTO assistant_draft_configs (",
            "  created_at, updated_at, user_id, org_id, assistant_id, prologue, instructions,",
            "  memory_config, knowledge_base_config, model_config, safety_config, vision_config,",
            "  rerank_config, recommend_config, recommend_questions",
            ") VALUES (",
            "  #{createdAt}, #{updatedAt}, #{userId}, #{orgId}, #{assistantId}, #{prologue}, #{instructions},",
            "  #{memoryConfigJson}, #{knowledgeBaseConfigJson}, #{modelConfigJson}, #{safetyConfigJson},",
            "  #{visionConfigJson}, #{rerankConfigJson}, #{recommendConfigJson}, #{recommendQuestionsJson}",
            ") ON DUPLICATE KEY UPDATE",
            "  updated_at = VALUES(updated_at),",
            "  prologue = VALUES(prologue),",
            "  instructions = VALUES(instructions),",
            "  memory_config = VALUES(memory_config),",
            "  knowledge_base_config = VALUES(knowledge_base_config),",
            "  model_config = VALUES(model_config),",
            "  safety_config = VALUES(safety_config),",
            "  vision_config = VALUES(vision_config),",
            "  rerank_config = VALUES(rerank_config),",
            "  recommend_config = VALUES(recommend_config),",
            "  recommend_questions = VALUES(recommend_questions)"
    })
    int upsert(AssistantDraftConfigEntity entity);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, assistant_id, prologue, instructions,",
            "       memory_config AS memory_config_json,",
            "       knowledge_base_config AS knowledge_base_config_json,",
            "       model_config AS model_config_json,",
            "       safety_config AS safety_config_json,",
            "       vision_config AS vision_config_json,",
            "       rerank_config AS rerank_config_json,",
            "       recommend_config AS recommend_config_json,",
            "       recommend_questions AS recommend_questions_json",
            "FROM assistant_draft_configs",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}",
            "LIMIT 1"
    })
    AssistantDraftConfigEntity selectByAssistant(@Param("userId") String userId,
                                                 @Param("orgId") String orgId,
                                                 @Param("assistantId") String assistantId);

    @Delete({
            "DELETE FROM assistant_draft_configs",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}"
    })
    int deleteByAssistant(@Param("userId") String userId,
                          @Param("orgId") String orgId,
                          @Param("assistantId") String assistantId);
}
