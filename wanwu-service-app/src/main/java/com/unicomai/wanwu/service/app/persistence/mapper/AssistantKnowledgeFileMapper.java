package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.AssistantKnowledgeFileEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssistantKnowledgeFileMapper extends BaseMapper<AssistantKnowledgeFileEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, assistant_id, file_id,",
            "       file_name, file_size, content_type, status, url",
            "FROM assistant_knowledge_files",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}",
            "ORDER BY id DESC"
    })
    List<AssistantKnowledgeFileEntity> selectByAssistant(@Param("userId") String userId,
                                                         @Param("orgId") String orgId,
                                                         @Param("assistantId") String assistantId);

    @Select({
            "SELECT COUNT(1)",
            "FROM assistant_knowledge_files",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}"
    })
    long countByAssistant(@Param("userId") String userId,
                          @Param("orgId") String orgId,
                          @Param("assistantId") String assistantId);

    @Delete({
            "DELETE FROM assistant_knowledge_files",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}",
            "  AND file_id = #{fileId}"
    })
    int deleteByAssistantFile(@Param("userId") String userId,
                              @Param("orgId") String orgId,
                              @Param("assistantId") String assistantId,
                              @Param("fileId") String fileId);

    @Delete({
            "DELETE FROM assistant_knowledge_files",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND file_id = #{fileId}"
    })
    int deleteByFile(@Param("userId") String userId,
                     @Param("orgId") String orgId,
                     @Param("fileId") String fileId);

    @Delete({
            "DELETE FROM assistant_knowledge_files",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND assistant_id = #{assistantId}"
    })
    int deleteByAssistant(@Param("userId") String userId,
                          @Param("orgId") String orgId,
                          @Param("assistantId") String assistantId);
}
