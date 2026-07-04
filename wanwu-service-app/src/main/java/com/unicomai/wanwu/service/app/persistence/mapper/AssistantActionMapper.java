package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantActionEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AssistantActionMapper extends BaseMapper<AssistantActionEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, assistant_id, action_id, name, payload",
            "FROM assistant_actions",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND action_id = #{actionId}"
    })
    AssistantActionEntity selectByAction(@Param("userId") String userId,
                                         @Param("orgId") String orgId,
                                         @Param("actionId") String actionId);

    @Update({
            "UPDATE assistant_actions",
            "SET updated_at = #{record.updatedAt},",
            "    assistant_id = #{record.assistantId},",
            "    name = #{record.name},",
            "    payload = #{record.payload}",
            "WHERE user_id = #{record.userId}",
            "  AND org_id = #{record.orgId}",
            "  AND action_id = #{record.actionId}"
    })
    int updateByAction(@Param("record") AssistantActionEntity record);

    @Delete({
            "DELETE FROM assistant_actions",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND action_id = #{actionId}"
    })
    int deleteByAction(@Param("userId") String userId,
                       @Param("orgId") String orgId,
                       @Param("actionId") String actionId);

    @Delete({
            "DELETE FROM assistant_actions",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}"
    })
    int deleteByAssistant(@Param("userId") String userId,
                          @Param("orgId") String orgId,
                          @Param("assistantId") String assistantId);
}
