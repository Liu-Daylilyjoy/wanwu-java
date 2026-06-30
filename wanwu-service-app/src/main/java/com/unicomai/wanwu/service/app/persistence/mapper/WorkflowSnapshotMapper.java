package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.WorkflowSnapshotEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WorkflowSnapshotMapper extends BaseMapper<WorkflowSnapshotEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, workflow_id, version,",
            "       snapshot_desc AS `desc`, category, workflow_info_json, workflow_schema_json",
            "FROM workflow_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND workflow_id = #{workflowId}",
            "ORDER BY created_at DESC, id DESC"
    })
    List<WorkflowSnapshotEntity> selectByWorkflow(@Param("userId") String userId,
                                                  @Param("orgId") String orgId,
                                                  @Param("workflowId") String workflowId);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, workflow_id, version,",
            "       snapshot_desc AS `desc`, category, workflow_info_json, workflow_schema_json",
            "FROM workflow_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND workflow_id = #{workflowId}",
            "ORDER BY created_at DESC, id DESC",
            "LIMIT 1"
    })
    WorkflowSnapshotEntity selectLatest(@Param("userId") String userId,
                                        @Param("orgId") String orgId,
                                        @Param("workflowId") String workflowId);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, workflow_id, version,",
            "       snapshot_desc AS `desc`, category, workflow_info_json, workflow_schema_json",
            "FROM workflow_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND workflow_id = #{workflowId}",
            "  AND version = #{version}",
            "LIMIT 1"
    })
    WorkflowSnapshotEntity selectByVersion(@Param("userId") String userId,
                                           @Param("orgId") String orgId,
                                           @Param("workflowId") String workflowId,
                                           @Param("version") String version);

    @Update({
            "UPDATE workflow_snapshots",
            "SET snapshot_desc = #{desc}, updated_at = #{updatedAt}",
            "WHERE id = #{id}"
    })
    int updateLatestDescription(@Param("id") Long id,
                                @Param("desc") String desc,
                                @Param("updatedAt") long updatedAt);

    @Delete({
            "DELETE FROM workflow_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND workflow_id = #{workflowId}"
    })
    int deleteByWorkflow(@Param("userId") String userId,
                         @Param("orgId") String orgId,
                         @Param("workflowId") String workflowId);
}
