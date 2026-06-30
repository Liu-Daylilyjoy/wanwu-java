package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.AppUrlEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AppUrlMapper extends BaseMapper<AppUrlEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, app_id, app_type,",
            "       name, description, expired_at, copyright, copyright_enable,",
            "       privacy_policy, privacy_policy_enable, disclaimer, disclaimer_enable,",
            "       suffix, status",
            "FROM app_urls",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_id = #{appId}",
            "  AND app_type = #{appType}",
            "ORDER BY id DESC"
    })
    List<AppUrlEntity> selectByApp(@Param("userId") String userId,
                                   @Param("orgId") String orgId,
                                   @Param("appId") String appId,
                                   @Param("appType") String appType);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, app_id, app_type,",
            "       name, description, expired_at, copyright, copyright_enable,",
            "       privacy_policy, privacy_policy_enable, disclaimer, disclaimer_enable,",
            "       suffix, status",
            "FROM app_urls",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND id = #{id}",
            "LIMIT 1"
    })
    AppUrlEntity selectByScopedId(@Param("userId") String userId,
                                  @Param("orgId") String orgId,
                                  @Param("id") Long id);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, app_id, app_type,",
            "       name, description, expired_at, copyright, copyright_enable,",
            "       privacy_policy, privacy_policy_enable, disclaimer, disclaimer_enable,",
            "       suffix, status",
            "FROM app_urls",
            "WHERE suffix = #{suffix}",
            "LIMIT 1"
    })
    AppUrlEntity selectBySuffix(@Param("suffix") String suffix);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, app_id, app_type,",
            "       name, description, expired_at, copyright, copyright_enable,",
            "       privacy_policy, privacy_policy_enable, disclaimer, disclaimer_enable,",
            "       suffix, status",
            "FROM app_urls",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_id = #{appId}",
            "  AND app_type = #{appType}",
            "  AND name = #{name}",
            "LIMIT 1"
    })
    AppUrlEntity selectByName(@Param("userId") String userId,
                              @Param("orgId") String orgId,
                              @Param("appId") String appId,
                              @Param("appType") String appType,
                              @Param("name") String name);

    @Update({
            "UPDATE app_urls",
            "SET updated_at = #{updatedAt},",
            "    name = #{name},",
            "    description = #{description},",
            "    expired_at = #{expiredAt},",
            "    copyright = #{copyright},",
            "    copyright_enable = #{copyrightEnable},",
            "    privacy_policy = #{privacyPolicy},",
            "    privacy_policy_enable = #{privacyPolicyEnable},",
            "    disclaimer = #{disclaimer},",
            "    disclaimer_enable = #{disclaimerEnable}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND id = #{id}"
    })
    int updateConfig(AppUrlEntity entity);

    @Update({
            "UPDATE app_urls",
            "SET updated_at = #{updatedAt}, status = #{status}",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND id = #{id}"
    })
    int updateStatus(@Param("userId") String userId,
                     @Param("orgId") String orgId,
                     @Param("id") Long id,
                     @Param("status") boolean status,
                     @Param("updatedAt") long updatedAt);

    @Delete({
            "DELETE FROM app_urls",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND id = #{id}"
    })
    int deleteByScopedId(@Param("userId") String userId,
                         @Param("orgId") String orgId,
                         @Param("id") Long id);

    @Delete({
            "DELETE FROM app_urls",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_id = #{assistantId}",
            "  AND app_type = 'agent'"
    })
    int deleteByAssistant(@Param("userId") String userId,
                          @Param("orgId") String orgId,
                          @Param("assistantId") String assistantId);
}
