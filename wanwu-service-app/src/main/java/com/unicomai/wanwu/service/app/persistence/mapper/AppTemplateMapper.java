package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.AppTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AppTemplateMapper extends BaseMapper<AppTemplateEntity> {

    @Select({
            "SELECT id, created_at, updated_at, template_type, template_id, category, name, description,",
            "       avatar_json, author, download_count, summary, feature, scenario, note,",
            "       prologue, instructions, recommend_questions_json, workflow_instruction, schema_json",
            "FROM app_templates",
            "WHERE template_type = #{templateType}",
            "  AND (#{category} IS NULL OR #{category} = '' OR #{category} = 'all' OR category = #{category})",
            "  AND (#{name} IS NULL OR #{name} = '' OR LOWER(name) LIKE CONCAT('%', LOWER(#{name}), '%'))",
            "ORDER BY id ASC"
    })
    List<AppTemplateEntity> selectByType(@Param("templateType") String templateType,
                                         @Param("category") String category,
                                         @Param("name") String name);

    @Select({
            "SELECT id, created_at, updated_at, template_type, template_id, category, name, description,",
            "       avatar_json, author, download_count, summary, feature, scenario, note,",
            "       prologue, instructions, recommend_questions_json, workflow_instruction, schema_json",
            "FROM app_templates",
            "WHERE template_type = #{templateType}",
            "  AND template_id = #{templateId}",
            "LIMIT 1"
    })
    AppTemplateEntity selectByTemplateId(@Param("templateType") String templateType,
                                         @Param("templateId") String templateId);
}
