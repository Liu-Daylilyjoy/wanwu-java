package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.RagSnapshotEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RagSnapshotMapper extends BaseMapper<RagSnapshotEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, rag_id, version,",
            "       snapshot_desc AS `desc`, category, rag_info_json, rag_config_json",
            "FROM rag_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND rag_id = #{ragId}",
            "ORDER BY created_at DESC, id DESC"
    })
    List<RagSnapshotEntity> selectByRag(@Param("userId") String userId,
                                        @Param("orgId") String orgId,
                                        @Param("ragId") String ragId);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, rag_id, version,",
            "       snapshot_desc AS `desc`, category, rag_info_json, rag_config_json",
            "FROM rag_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND rag_id = #{ragId}",
            "ORDER BY created_at DESC, id DESC",
            "LIMIT 1"
    })
    RagSnapshotEntity selectLatest(@Param("userId") String userId,
                                   @Param("orgId") String orgId,
                                   @Param("ragId") String ragId);

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, rag_id, version,",
            "       snapshot_desc AS `desc`, category, rag_info_json, rag_config_json",
            "FROM rag_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND rag_id = #{ragId}",
            "  AND version = #{version}",
            "LIMIT 1"
    })
    RagSnapshotEntity selectByVersion(@Param("userId") String userId,
                                      @Param("orgId") String orgId,
                                      @Param("ragId") String ragId,
                                      @Param("version") String version);

    @Update({
            "UPDATE rag_snapshots",
            "SET snapshot_desc = #{desc}, updated_at = #{updatedAt}",
            "WHERE id = #{id}"
    })
    int updateLatestDescription(@Param("id") Long id,
                                @Param("desc") String desc,
                                @Param("updatedAt") long updatedAt);

    @Delete({
            "DELETE FROM rag_snapshots",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND rag_id = #{ragId}"
    })
    int deleteByRag(@Param("userId") String userId,
                    @Param("orgId") String orgId,
                    @Param("ragId") String ragId);
}
