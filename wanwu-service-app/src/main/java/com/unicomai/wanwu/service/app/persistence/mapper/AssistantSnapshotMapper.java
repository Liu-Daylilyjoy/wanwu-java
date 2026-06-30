package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantSnapshotEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AssistantSnapshotMapper extends BaseMapper<AssistantSnapshotEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, assistant_id, version,",
            "       snapshot_desc AS `desc`, category, assistant_info_json, assistant_config_json",
            "FROM assistant_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}",
            "ORDER BY created_at DESC, id DESC"
    })
    List<AssistantSnapshotEntity> selectByAssistant(@Param("userId") String userId,
                                                    @Param("orgId") String orgId,
                                                    @Param("assistantId") String assistantId);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, assistant_id, version,",
            "       snapshot_desc AS `desc`, category, assistant_info_json, assistant_config_json",
            "FROM assistant_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}",
            "ORDER BY created_at DESC, id DESC",
            "LIMIT 1"
    })
    AssistantSnapshotEntity selectLatest(@Param("userId") String userId,
                                         @Param("orgId") String orgId,
                                         @Param("assistantId") String assistantId);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, assistant_id, version,",
            "       snapshot_desc AS `desc`, category, assistant_info_json, assistant_config_json",
            "FROM assistant_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}",
            "  AND version = #{version}",
            "LIMIT 1"
    })
    AssistantSnapshotEntity selectByVersion(@Param("userId") String userId,
                                            @Param("orgId") String orgId,
                                            @Param("assistantId") String assistantId,
                                            @Param("version") String version);

    @Update({
            "UPDATE assistant_snapshots",
            "SET snapshot_desc = #{desc}, updated_at = #{updatedAt}",
            "WHERE id = #{id}"
    })
    int updateLatestDescription(@Param("id") Long id,
                                @Param("desc") String desc,
                                @Param("updatedAt") long updatedAt);

    @Delete({
            "DELETE FROM assistant_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}"
    })
    int deleteByAssistant(@Param("userId") String userId,
                          @Param("orgId") String orgId,
                          @Param("assistantId") String assistantId);
}
