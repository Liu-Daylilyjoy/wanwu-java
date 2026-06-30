package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantConversationEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AssistantConversationMapper extends BaseMapper<AssistantConversationEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, assistant_id,",
            "       conversation_id, conversation_type, title",
            "FROM assistant_conversations",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}",
            "  AND conversation_type = #{conversationType}",
            "ORDER BY created_at DESC, id DESC",
            "LIMIT #{limit} OFFSET #{offset}"
    })
    List<AssistantConversationEntity> selectPage(@Param("userId") String userId,
                                                 @Param("orgId") String orgId,
                                                 @Param("assistantId") String assistantId,
                                                 @Param("conversationType") String conversationType,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    @Select({
            "SELECT COUNT(1)",
            "FROM assistant_conversations",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}",
            "  AND conversation_type = #{conversationType}"
    })
    long countByAssistant(@Param("userId") String userId,
                          @Param("orgId") String orgId,
                          @Param("assistantId") String assistantId,
                          @Param("conversationType") String conversationType);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, assistant_id,",
            "       conversation_id, conversation_type, title",
            "FROM assistant_conversations",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND conversation_id = #{conversationId}",
            "LIMIT 1"
    })
    AssistantConversationEntity selectByConversationId(@Param("userId") String userId,
                                                       @Param("orgId") String orgId,
                                                       @Param("conversationId") String conversationId);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, assistant_id,",
            "       conversation_id, conversation_type, title",
            "FROM assistant_conversations",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}",
            "  AND conversation_type = 'draft'",
            "ORDER BY created_at ASC, id ASC",
            "LIMIT 1"
    })
    AssistantConversationEntity selectDraftByAssistant(@Param("userId") String userId,
                                                       @Param("orgId") String orgId,
                                                       @Param("assistantId") String assistantId);

    @Update({
            "UPDATE assistant_conversations",
            "SET updated_at = #{updatedAt}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND conversation_id = #{conversationId}"
    })
    int touch(@Param("userId") String userId,
              @Param("orgId") String orgId,
              @Param("conversationId") String conversationId,
              @Param("updatedAt") long updatedAt);

    @Delete({
            "DELETE FROM assistant_conversations",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND conversation_id = #{conversationId}"
    })
    int deleteByConversationId(@Param("userId") String userId,
                               @Param("orgId") String orgId,
                               @Param("conversationId") String conversationId);

    @Delete({
            "DELETE FROM assistant_conversations",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}"
    })
    int deleteByAssistant(@Param("userId") String userId,
                          @Param("orgId") String orgId,
                          @Param("assistantId") String assistantId);
}
