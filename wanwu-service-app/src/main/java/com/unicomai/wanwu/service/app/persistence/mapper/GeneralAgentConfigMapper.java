package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.GeneralAgentConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GeneralAgentConfigMapper extends BaseMapper<GeneralAgentConfigEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, config_json",
            "FROM general_agent_configs",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}"
    })
    GeneralAgentConfigEntity selectByScope(@Param("userId") String userId,
                                           @Param("orgId") String orgId);

    @Update({
            "UPDATE general_agent_configs",
            "SET updated_at = #{record.updatedAt},",
            "    config_json = #{record.configJson}",
            "WHERE user_id = #{record.userId}",
            "  AND org_id = #{record.orgId}"
    })
    int updateByScope(@Param("record") GeneralAgentConfigEntity record);
}
