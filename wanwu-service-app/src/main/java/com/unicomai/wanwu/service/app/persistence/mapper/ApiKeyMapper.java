package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.ApiKeyEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ApiKeyMapper extends BaseMapper<ApiKeyEntity> {

    @Select({
            "SELECT id, created_at, updated_at, org_id, user_id, `key`, description, name, status, expired_at",
            "FROM open_api_keys",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND name = #{name}",
            "LIMIT 1"
    })
    ApiKeyEntity selectByScopedName(@Param("userId") String userId,
                                    @Param("orgId") String orgId,
                                    @Param("name") String name);

    @Select({
            "SELECT id, created_at, updated_at, org_id, user_id, `key`, description, name, status, expired_at",
            "FROM open_api_keys",
            "WHERE id = #{id}",
            "LIMIT 1"
    })
    ApiKeyEntity selectByIdValue(@Param("id") Long id);

    @Select({
            "SELECT id, created_at, updated_at, org_id, user_id, `key`, description, name, status, expired_at",
            "FROM open_api_keys",
            "WHERE `key` = #{key}",
            "LIMIT 1"
    })
    ApiKeyEntity selectByKey(@Param("key") String key);

    @Select({
            "SELECT id, created_at, updated_at, org_id, user_id, `key`, description, name, status, expired_at",
            "FROM open_api_keys",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "ORDER BY id DESC",
            "LIMIT #{limit} OFFSET #{offset}"
    })
    List<ApiKeyEntity> selectPage(@Param("userId") String userId,
                                  @Param("orgId") String orgId,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    @Select({
            "SELECT COUNT(*)",
            "FROM open_api_keys",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}"
    })
    long countByUser(@Param("userId") String userId, @Param("orgId") String orgId);

    @Update({
            "UPDATE open_api_keys",
            "SET updated_at = #{updatedAt},",
            "    name = #{name},",
            "    description = #{description},",
            "    expired_at = #{expiredAt}",
            "WHERE id = #{id}"
    })
    int updateConfig(ApiKeyEntity entity);

    @Update({
            "UPDATE open_api_keys",
            "SET updated_at = #{updatedAt}, status = #{status}",
            "WHERE id = #{id}"
    })
    int updateStatus(@Param("id") Long id,
                     @Param("status") boolean status,
                     @Param("updatedAt") long updatedAt);

    @Delete({
            "DELETE FROM open_api_keys",
            "WHERE id = #{id}"
    })
    int deleteByIdValue(@Param("id") Long id);
}
