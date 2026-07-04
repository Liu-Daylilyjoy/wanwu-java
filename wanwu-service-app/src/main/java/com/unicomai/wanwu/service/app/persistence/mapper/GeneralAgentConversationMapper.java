package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.GeneralAgentConversationEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GeneralAgentConversationMapper extends BaseMapper<GeneralAgentConversationEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, thread_id, title, skill_conversation,",
            "       skill_id, preview_id, model_config_json, runs_json",
            "FROM general_agent_conversations",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND thread_id = #{threadId}"
    })
    GeneralAgentConversationEntity selectByThread(@Param("userId") String userId,
                                                  @Param("orgId") String orgId,
                                                  @Param("threadId") String threadId);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, thread_id, title, skill_conversation,",
            "       skill_id, preview_id, model_config_json, runs_json",
            "FROM general_agent_conversations",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND preview_id = #{previewId}",
            "ORDER BY updated_at DESC, id DESC",
            "LIMIT 1"
    })
    GeneralAgentConversationEntity selectByPreview(@Param("userId") String userId,
                                                   @Param("orgId") String orgId,
                                                   @Param("previewId") String previewId);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, thread_id, title, skill_conversation,",
            "       skill_id, preview_id, model_config_json, runs_json",
            "FROM general_agent_conversations",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "ORDER BY updated_at DESC, id DESC"
    })
    List<GeneralAgentConversationEntity> selectByScope(@Param("userId") String userId,
                                                       @Param("orgId") String orgId);

    @Update({
            "UPDATE general_agent_conversations",
            "SET updated_at = #{record.updatedAt},",
            "    title = #{record.title},",
            "    skill_conversation = #{record.skillConversation},",
            "    skill_id = #{record.skillId},",
            "    preview_id = #{record.previewId},",
            "    model_config_json = #{record.modelConfigJson},",
            "    runs_json = #{record.runsJson}",
            "WHERE user_id = #{record.userId}",
            "  AND org_id = #{record.orgId}",
            "  AND thread_id = #{record.threadId}"
    })
    int updateByThread(@Param("record") GeneralAgentConversationEntity record);

    @Delete({
            "DELETE FROM general_agent_conversations",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND thread_id = #{threadId}"
    })
    int deleteByThread(@Param("userId") String userId,
                       @Param("orgId") String orgId,
                       @Param("threadId") String threadId);
}
