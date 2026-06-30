package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.RagDraftConfigEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RagDraftConfigMapper extends BaseMapper<RagDraftConfigEntity> {

    @Insert({
            "INSERT INTO rag_draft_configs (",
            "  created_at, updated_at, user_id, org_id, rag_id, model_config, rerank_config,",
            "  qa_rerank_config, knowledge_base_config, qa_knowledge_base_config, safety_config, vision_config",
            ") VALUES (",
            "  #{createdAt}, #{updatedAt}, #{userId}, #{orgId}, #{ragId}, #{modelConfigJson}, #{rerankConfigJson},",
            "  #{qaRerankConfigJson}, #{knowledgeBaseConfigJson}, #{qaKnowledgeBaseConfigJson},",
            "  #{safetyConfigJson}, #{visionConfigJson}",
            ") ON DUPLICATE KEY UPDATE",
            "  updated_at = VALUES(updated_at),",
            "  model_config = VALUES(model_config),",
            "  rerank_config = VALUES(rerank_config),",
            "  qa_rerank_config = VALUES(qa_rerank_config),",
            "  knowledge_base_config = VALUES(knowledge_base_config),",
            "  qa_knowledge_base_config = VALUES(qa_knowledge_base_config),",
            "  safety_config = VALUES(safety_config),",
            "  vision_config = VALUES(vision_config)"
    })
    int upsert(RagDraftConfigEntity entity);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, rag_id,",
            "       model_config AS model_config_json,",
            "       rerank_config AS rerank_config_json,",
            "       qa_rerank_config AS qa_rerank_config_json,",
            "       knowledge_base_config AS knowledge_base_config_json,",
            "       qa_knowledge_base_config AS qa_knowledge_base_config_json,",
            "       safety_config AS safety_config_json,",
            "       vision_config AS vision_config_json",
            "FROM rag_draft_configs",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND rag_id = #{ragId}",
            "LIMIT 1"
    })
    RagDraftConfigEntity selectByRag(@Param("userId") String userId,
                                     @Param("orgId") String orgId,
                                     @Param("ragId") String ragId);

    @Delete({
            "DELETE FROM rag_draft_configs",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND rag_id = #{ragId}"
    })
    int deleteByRag(@Param("userId") String userId,
                    @Param("orgId") String orgId,
                    @Param("ragId") String ragId);
}
