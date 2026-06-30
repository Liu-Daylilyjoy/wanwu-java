package com.unicomai.wanwu.service.mcp.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.mcp.persistence.entity.McpRecordEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface McpRecordMapper extends BaseMapper<McpRecordEntity> {

    @Select({
            "SELECT id, record_type, record_id, payload, created_at, updated_at",
            "FROM mcp_records",
            "WHERE record_type = #{recordType}",
            "ORDER BY id ASC"
    })
    List<McpRecordEntity> selectByType(@Param("recordType") String recordType);

    @Insert({
            "INSERT INTO mcp_records (record_type, record_id, payload, created_at, updated_at)",
            "VALUES (#{recordType}, #{recordId}, #{payload}, #{createdAt}, #{updatedAt})",
            "ON DUPLICATE KEY UPDATE",
            "payload = VALUES(payload),",
            "updated_at = VALUES(updated_at)"
    })
    int upsertRecord(McpRecordEntity entity);
}
