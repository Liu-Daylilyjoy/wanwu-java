package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantDraftEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AssistantDraftMapper extends BaseMapper<AssistantDraftEntity> {

    @Update({
            "UPDATE assistant_drafts",
            "SET updated_at = #{updatedAt},",
            "    name = #{name},",
            "    description = #{desc},",
            "    avatar_key = #{avatarKey},",
            "    avatar_path = #{avatarPath},",
            "    category = #{category}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{appId}"
    })
    int updateDraft(AppRecord record);

    @Delete({
            "DELETE FROM assistant_drafts",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}"
    })
    int deleteDraft(@Param("userId") String userId,
                    @Param("orgId") String orgId,
                    @Param("assistantId") String assistantId);
}
