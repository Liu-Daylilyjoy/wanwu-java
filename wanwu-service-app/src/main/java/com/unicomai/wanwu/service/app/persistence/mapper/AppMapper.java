package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.domain.AppRecord;
import com.unicomai.wanwu.service.app.persistence.entity.AppEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AppMapper extends BaseMapper<AppEntity> {

    @Select({
            "<script>",
            "SELECT a.id, a.created_at, a.updated_at, a.user_id, a.org_id,",
            "       a.app_id, a.app_type, a.publish_type,",
            "       d.name, d.description AS `desc`, d.avatar_key, d.avatar_path, d.category",
            "FROM apps a",
            "JOIN assistant_drafts d ON d.assistant_id = a.app_id",
            "WHERE a.user_id = #{userId}",
            "  AND a.org_id = #{orgId}",
            "  AND a.app_type = 'agent'",
            "<if test='name != null and name != \"\"'>",
            "  AND d.name LIKE CONCAT('%', #{name}, '%')",
            "</if>",
            "ORDER BY a.id DESC",
            "</script>"
    })
    List<AppRecord> selectAssistantRecords(@Param("userId") String userId,
                                           @Param("orgId") String orgId,
                                           @Param("name") String name);

    @Select({
            "SELECT a.id, a.created_at, a.updated_at, a.user_id, a.org_id,",
            "       a.app_id, a.app_type, a.publish_type,",
            "       d.name, d.description AS `desc`, d.avatar_key, d.avatar_path, d.category",
            "FROM apps a",
            "JOIN assistant_drafts d ON d.assistant_id = a.app_id",
            "WHERE a.user_id = #{userId}",
            "  AND a.org_id = #{orgId}",
            "  AND a.app_type = 'agent'",
            "  AND a.app_id = #{assistantId}",
            "LIMIT 1"
    })
    AppRecord selectAssistantRecord(@Param("userId") String userId,
                                    @Param("orgId") String orgId,
                                    @Param("assistantId") String assistantId);

    @Select({
            "SELECT d.name",
            "FROM apps a",
            "JOIN assistant_drafts d ON d.assistant_id = a.app_id",
            "WHERE a.user_id = #{userId}",
            "  AND a.org_id = #{orgId}",
            "  AND a.app_type = 'agent'",
            "  AND d.name LIKE CONCAT(#{prefix}, '%')"
    })
    List<String> selectAssistantNamesByPrefix(@Param("userId") String userId,
                                              @Param("orgId") String orgId,
                                              @Param("prefix") String prefix);

    @Update({
            "UPDATE apps",
            "SET updated_at = #{updatedAt}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_type = 'agent'",
            "  AND app_id = #{assistantId}"
    })
    int updateAssistantUpdatedAt(@Param("userId") String userId,
                                 @Param("orgId") String orgId,
                                 @Param("assistantId") String assistantId,
                                 @Param("updatedAt") Long updatedAt);

    @Delete({
            "DELETE FROM apps",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_type = 'agent'",
            "  AND app_id = #{assistantId}"
    })
    int deleteAssistantApp(@Param("userId") String userId,
                           @Param("orgId") String orgId,
                           @Param("assistantId") String assistantId);
}
