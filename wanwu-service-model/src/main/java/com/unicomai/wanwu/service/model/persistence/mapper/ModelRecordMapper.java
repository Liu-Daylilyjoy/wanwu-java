package com.unicomai.wanwu.service.model.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.model.persistence.entity.ModelRecordEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ModelRecordMapper extends BaseMapper<ModelRecordEntity> {

    @Select({
            "SELECT id, record_type, record_id, payload, created_at, updated_at",
            "FROM model_records",
            "WHERE record_type = #{recordType}",
            "ORDER BY id ASC"
    })
    List<ModelRecordEntity> selectByType(@Param("recordType") String recordType);

    @Insert({
            "INSERT INTO model_records (record_type, record_id, payload, created_at, updated_at)",
            "VALUES (#{recordType}, #{recordId}, #{payload}, #{createdAt}, #{updatedAt})",
            "ON DUPLICATE KEY UPDATE",
            "payload = VALUES(payload),",
            "updated_at = VALUES(updated_at)"
    })
    int upsertRecord(ModelRecordEntity entity);

    @Delete({
            "DELETE FROM model_records",
            "WHERE record_type = #{recordType}",
            "  AND record_id = #{recordId}"
    })
    int deleteRecord(@Param("recordType") String recordType, @Param("recordId") String recordId);
}
