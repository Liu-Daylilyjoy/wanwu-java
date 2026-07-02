package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.ApiKeyUsageRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApiKeyUsageRecordMapper extends BaseMapper<ApiKeyUsageRecordEntity> {

    @Select({
            "<script>",
            "SELECT id, created_at, updated_at, org_id, user_id, api_key_id, method_path,",
            "       call_time, response_status, is_stream, stream_costs, non_stream_costs,",
            "       request_body, response_body, date",
            "FROM api_key_records",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND date <![CDATA[>=]]> #{startDate}",
            "  AND date <![CDATA[<=]]> #{endDate}",
            "<if test='apiKeyIds != null and apiKeyIds.size > 0'>",
            "  AND api_key_id IN",
            "  <foreach collection='apiKeyIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "</if>",
            "<if test='methodPaths != null and methodPaths.size > 0'>",
            "  AND method_path IN",
            "  <foreach collection='methodPaths' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "</if>",
            "ORDER BY call_time DESC, id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<ApiKeyUsageRecordEntity> selectPage(@Param("userId") String userId,
                                             @Param("orgId") String orgId,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate,
                                             @Param("apiKeyIds") List<String> apiKeyIds,
                                             @Param("methodPaths") List<String> methodPaths,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT COUNT(*)",
            "FROM api_key_records",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND date <![CDATA[>=]]> #{startDate}",
            "  AND date <![CDATA[<=]]> #{endDate}",
            "<if test='apiKeyIds != null and apiKeyIds.size > 0'>",
            "  AND api_key_id IN",
            "  <foreach collection='apiKeyIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "</if>",
            "<if test='methodPaths != null and methodPaths.size > 0'>",
            "  AND method_path IN",
            "  <foreach collection='methodPaths' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "</if>",
            "</script>"
    })
    long countRecords(@Param("userId") String userId,
                      @Param("orgId") String orgId,
                      @Param("startDate") String startDate,
                      @Param("endDate") String endDate,
                      @Param("apiKeyIds") List<String> apiKeyIds,
                      @Param("methodPaths") List<String> methodPaths);
}
