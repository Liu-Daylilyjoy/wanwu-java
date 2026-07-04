package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.WorkflowRunEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkflowRunMapper extends BaseMapper<WorkflowRunEntity> {

    @Select({
            "SELECT id, created_at, updated_at, finished_at, user_id, org_id, workflow_id,",
            "       run_id, status, input_json, output_json, cost_millis",
            "FROM workflow_run_records",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND workflow_id = #{workflowId}",
            "ORDER BY created_at DESC, id DESC",
            "LIMIT #{limit}"
    })
    List<WorkflowRunEntity> selectByWorkflow(@Param("userId") String userId,
                                             @Param("orgId") String orgId,
                                             @Param("workflowId") String workflowId,
                                             @Param("limit") int limit);

    @Delete({
            "DELETE FROM workflow_run_records",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND workflow_id = #{workflowId}"
    })
    int deleteByWorkflow(@Param("userId") String userId,
                         @Param("orgId") String orgId,
                         @Param("workflowId") String workflowId);
}
