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
            "SELECT a.id, a.created_at, a.updated_at, a.user_id, a.org_id,",
            "       a.app_id, a.app_type, a.publish_type,",
            "       d.name, d.description AS `desc`, d.avatar_key, d.avatar_path, d.category",
            "FROM apps a",
            "JOIN assistant_drafts d ON d.assistant_id = a.app_id",
            "WHERE a.org_id = #{orgId}",
            "  AND a.app_type = 'agent'",
            "  AND a.app_id = #{assistantId}",
            "LIMIT 1"
    })
    AppRecord selectAssistantRecordByOrg(@Param("orgId") String orgId,
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

    @Select({
            "<script>",
            "SELECT a.id, a.created_at, a.updated_at, a.user_id, a.org_id,",
            "       a.app_id, a.app_type, a.publish_type,",
            "       d.name, d.description AS `desc`, d.avatar_key, d.avatar_path, d.category",
            "FROM apps a",
            "JOIN rag_drafts d ON d.rag_id = a.app_id",
            "WHERE a.user_id = #{userId}",
            "  AND a.org_id = #{orgId}",
            "  AND a.app_type = 'rag'",
            "<if test='name != null and name != \"\"'>",
            "  AND d.name LIKE CONCAT('%', #{name}, '%')",
            "</if>",
            "ORDER BY a.id DESC",
            "</script>"
    })
    List<AppRecord> selectRagRecords(@Param("userId") String userId,
                                     @Param("orgId") String orgId,
                                     @Param("name") String name);

    @Select({
            "SELECT a.id, a.created_at, a.updated_at, a.user_id, a.org_id,",
            "       a.app_id, a.app_type, a.publish_type,",
            "       d.name, d.description AS `desc`, d.avatar_key, d.avatar_path, d.category",
            "FROM apps a",
            "JOIN rag_drafts d ON d.rag_id = a.app_id",
            "WHERE a.user_id = #{userId}",
            "  AND a.org_id = #{orgId}",
            "  AND a.app_type = 'rag'",
            "  AND a.app_id = #{ragId}",
            "LIMIT 1"
    })
    AppRecord selectRagRecord(@Param("userId") String userId,
                              @Param("orgId") String orgId,
                              @Param("ragId") String ragId);

    @Select({
            "SELECT d.name",
            "FROM apps a",
            "JOIN rag_drafts d ON d.rag_id = a.app_id",
            "WHERE a.user_id = #{userId}",
            "  AND a.org_id = #{orgId}",
            "  AND a.app_type = 'rag'",
            "  AND d.name LIKE CONCAT(#{prefix}, '%')"
    })
    List<String> selectRagNamesByPrefix(@Param("userId") String userId,
                                        @Param("orgId") String orgId,
                                        @Param("prefix") String prefix);

    @Select({
            "<script>",
            "SELECT a.id, a.created_at, a.updated_at, a.user_id, a.org_id,",
            "       a.app_id, a.app_type, a.publish_type,",
            "       d.name, d.description AS `desc`, d.avatar_key, d.avatar_path, d.category",
            "FROM apps a",
            "JOIN workflow_drafts d ON d.workflow_id = a.app_id",
            "WHERE a.user_id = #{userId}",
            "  AND a.org_id = #{orgId}",
            "  AND a.app_type = #{appType}",
            "<if test='name != null and name != \"\"'>",
            "  AND d.name LIKE CONCAT('%', #{name}, '%')",
            "</if>",
            "ORDER BY a.id DESC",
            "</script>"
    })
    List<AppRecord> selectWorkflowRecords(@Param("userId") String userId,
                                          @Param("orgId") String orgId,
                                          @Param("name") String name,
                                          @Param("appType") String appType);

    @Select({
            "SELECT a.id, a.created_at, a.updated_at, a.user_id, a.org_id,",
            "       a.app_id, a.app_type, a.publish_type,",
            "       d.name, d.description AS `desc`, d.avatar_key, d.avatar_path, d.category",
            "FROM apps a",
            "JOIN workflow_drafts d ON d.workflow_id = a.app_id",
            "WHERE a.user_id = #{userId}",
            "  AND a.org_id = #{orgId}",
            "  AND a.app_type = #{appType}",
            "  AND a.app_id = #{workflowId}",
            "LIMIT 1"
    })
    AppRecord selectWorkflowRecord(@Param("userId") String userId,
                                   @Param("orgId") String orgId,
                                   @Param("workflowId") String workflowId,
                                   @Param("appType") String appType);

    @Select({
            "SELECT d.name",
            "FROM apps a",
            "JOIN workflow_drafts d ON d.workflow_id = a.app_id",
            "WHERE a.user_id = #{userId}",
            "  AND a.org_id = #{orgId}",
            "  AND a.app_type = #{appType}",
            "  AND d.name LIKE CONCAT(#{prefix}, '%')"
    })
    List<String> selectWorkflowNamesByPrefix(@Param("userId") String userId,
                                             @Param("orgId") String orgId,
                                             @Param("prefix") String prefix,
                                             @Param("appType") String appType);

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

    @Update({
            "UPDATE apps",
            "SET publish_type = #{publishType}, updated_at = #{updatedAt}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_type = 'agent'",
            "  AND app_id = #{assistantId}"
    })
    int updateAssistantPublishType(@Param("userId") String userId,
                                   @Param("orgId") String orgId,
                                   @Param("assistantId") String assistantId,
                                   @Param("publishType") String publishType,
                                   @Param("updatedAt") Long updatedAt);

    @Update({
            "UPDATE apps",
            "SET updated_at = #{updatedAt}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_type = 'rag'",
            "  AND app_id = #{ragId}"
    })
    int updateRagUpdatedAt(@Param("userId") String userId,
                           @Param("orgId") String orgId,
                           @Param("ragId") String ragId,
                           @Param("updatedAt") Long updatedAt);

    @Update({
            "UPDATE apps",
            "SET publish_type = #{publishType}, updated_at = #{updatedAt}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_type = 'rag'",
            "  AND app_id = #{ragId}"
    })
    int updateRagPublishType(@Param("userId") String userId,
                             @Param("orgId") String orgId,
                             @Param("ragId") String ragId,
                             @Param("publishType") String publishType,
                             @Param("updatedAt") Long updatedAt);

    @Update({
            "UPDATE apps",
            "SET updated_at = #{updatedAt}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_type = #{appType}",
            "  AND app_id = #{workflowId}"
    })
    int updateWorkflowUpdatedAt(@Param("userId") String userId,
                                @Param("orgId") String orgId,
                                @Param("workflowId") String workflowId,
                                @Param("appType") String appType,
                                @Param("updatedAt") Long updatedAt);

    @Update({
            "UPDATE apps",
            "SET publish_type = #{publishType}, updated_at = #{updatedAt}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_type = #{appType}",
            "  AND app_id = #{workflowId}"
    })
    int updateWorkflowPublishType(@Param("userId") String userId,
                                  @Param("orgId") String orgId,
                                  @Param("workflowId") String workflowId,
                                  @Param("appType") String appType,
                                  @Param("publishType") String publishType,
                                  @Param("updatedAt") Long updatedAt);

    @Update({
            "UPDATE apps",
            "SET app_type = #{newAppType}, updated_at = #{updatedAt}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_type = #{oldAppType}",
            "  AND app_id = #{workflowId}"
    })
    int updateWorkflowAppType(@Param("userId") String userId,
                              @Param("orgId") String orgId,
                              @Param("workflowId") String workflowId,
                              @Param("oldAppType") String oldAppType,
                              @Param("newAppType") String newAppType,
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

    @Delete({
            "DELETE FROM apps",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_type = 'rag'",
            "  AND app_id = #{ragId}"
    })
    int deleteRagApp(@Param("userId") String userId,
                     @Param("orgId") String orgId,
                     @Param("ragId") String ragId);

    @Delete({
            "DELETE FROM apps",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_type = #{appType}",
            "  AND app_id = #{workflowId}"
    })
    int deleteWorkflowApp(@Param("userId") String userId,
                          @Param("orgId") String orgId,
                          @Param("workflowId") String workflowId,
                          @Param("appType") String appType);
}
