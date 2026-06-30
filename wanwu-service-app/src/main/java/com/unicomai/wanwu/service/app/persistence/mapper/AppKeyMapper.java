package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.AppKeyEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AppKeyMapper extends BaseMapper<AppKeyEntity> {

    @Select({
            "SELECT id, created_at, updated_at, org_id, user_id, app_id, app_type, api_key",
            "FROM api_keys",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND app_id = #{appId}",
            "  AND app_type = #{appType}",
            "ORDER BY id DESC"
    })
    List<AppKeyEntity> selectByApp(@Param("userId") String userId,
                                   @Param("orgId") String orgId,
                                   @Param("appId") String appId,
                                   @Param("appType") String appType);

    @Select({
            "SELECT id, created_at, updated_at, org_id, user_id, app_id, app_type, api_key",
            "FROM api_keys",
            "WHERE id = #{id}",
            "LIMIT 1"
    })
    AppKeyEntity selectByIdValue(@Param("id") Long id);

    @Select({
            "SELECT id, created_at, updated_at, org_id, user_id, app_id, app_type, api_key",
            "FROM api_keys",
            "WHERE api_key = #{apiKey}",
            "LIMIT 1"
    })
    AppKeyEntity selectByApiKey(@Param("apiKey") String apiKey);

    @Delete({
            "DELETE FROM api_keys",
            "WHERE id = #{id}"
    })
    int deleteByIdValue(@Param("id") Long id);
}
