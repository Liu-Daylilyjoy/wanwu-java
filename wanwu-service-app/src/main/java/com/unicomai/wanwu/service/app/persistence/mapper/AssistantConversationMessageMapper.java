package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantConversationMessageEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssistantConversationMessageMapper extends BaseMapper<AssistantConversationMessageEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, assistant_id, conversation_id, detail_id,",
            "       prompt, sys_prompt, response, response_list, search_list, request_files,",
            "       response_files, sub_conversation_list, file_size, file_name, qa_type",
            "FROM assistant_conversation_messages",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND conversation_id = #{conversationId}",
            "ORDER BY created_at ASC, id ASC",
            "LIMIT #{limit} OFFSET #{offset}"
    })
    List<AssistantConversationMessageEntity> selectPage(@Param("userId") String userId,
                                                        @Param("orgId") String orgId,
                                                        @Param("conversationId") String conversationId,
                                                        @Param("offset") int offset,
                                                        @Param("limit") int limit);

    @Select({
            "SELECT COUNT(1)",
            "FROM assistant_conversation_messages",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND conversation_id = #{conversationId}"
    })
    long countByConversation(@Param("userId") String userId,
                             @Param("orgId") String orgId,
                             @Param("conversationId") String conversationId);

    @Delete({
            "DELETE FROM assistant_conversation_messages",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND conversation_id = #{conversationId}",
            "  AND detail_id = #{detailId}"
    })
    int deleteDetail(@Param("userId") String userId,
                     @Param("orgId") String orgId,
                     @Param("conversationId") String conversationId,
                     @Param("detailId") String detailId);

    @Delete({
            "DELETE FROM assistant_conversation_messages",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND conversation_id = #{conversationId}"
    })
    int deleteByConversation(@Param("userId") String userId,
                             @Param("orgId") String orgId,
                             @Param("conversationId") String conversationId);

    @Delete({
            "DELETE FROM assistant_conversation_messages",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}"
    })
    int deleteByAssistant(@Param("userId") String userId,
                          @Param("orgId") String orgId,
                          @Param("assistantId") String assistantId);
}
