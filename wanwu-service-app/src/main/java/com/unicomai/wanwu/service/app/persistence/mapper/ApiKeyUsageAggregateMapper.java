package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.ApiKeyUsageAggregateEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApiKeyUsageAggregateMapper extends BaseMapper<ApiKeyUsageAggregateEntity> {

    @Insert({
            "INSERT INTO api_key_statistics (",
            "  created_at, updated_at, org_id, user_id, api_key_id, method_path, date,",
            "  call_count, call_failure, stream_count, non_stream_count,",
            "  stream_failure, non_stream_failure, stream_costs, non_stream_costs",
            ") VALUES (",
            "  #{createdAt}, #{updatedAt}, #{orgId}, #{userId}, #{apiKeyId}, #{methodPath}, #{date},",
            "  #{callCount}, #{callFailure}, #{streamCount}, #{nonStreamCount},",
            "  #{streamFailure}, #{nonStreamFailure}, #{streamCosts}, #{nonStreamCosts}",
            ") ON DUPLICATE KEY UPDATE",
            "  updated_at = VALUES(updated_at),",
            "  call_count = call_count + VALUES(call_count),",
            "  call_failure = call_failure + VALUES(call_failure),",
            "  stream_count = stream_count + VALUES(stream_count),",
            "  non_stream_count = non_stream_count + VALUES(non_stream_count),",
            "  stream_failure = stream_failure + VALUES(stream_failure),",
            "  non_stream_failure = non_stream_failure + VALUES(non_stream_failure),",
            "  stream_costs = stream_costs + VALUES(stream_costs),",
            "  non_stream_costs = non_stream_costs + VALUES(non_stream_costs)"
    })
    int upsertDelta(ApiKeyUsageAggregateEntity entity);

    @Select({
            "<script>",
            "SELECT",
            "  COALESCE(SUM(call_count), 0) AS call_count,",
            "  COALESCE(SUM(call_failure), 0) AS call_failure,",
            "  COALESCE(SUM(stream_count), 0) AS stream_count,",
            "  COALESCE(SUM(non_stream_count), 0) AS non_stream_count,",
            "  COALESCE(SUM(stream_failure), 0) AS stream_failure,",
            "  COALESCE(SUM(non_stream_failure), 0) AS non_stream_failure,",
            "  COALESCE(SUM(stream_costs), 0) AS stream_costs,",
            "  COALESCE(SUM(non_stream_costs), 0) AS non_stream_costs",
            "FROM api_key_statistics",
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
    ApiKeyUsageAggregateEntity selectSum(@Param("userId") String userId,
                                         @Param("orgId") String orgId,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("apiKeyIds") List<String> apiKeyIds,
                                         @Param("methodPaths") List<String> methodPaths);

    @Select({
            "<script>",
            "SELECT date,",
            "       COALESCE(SUM(call_count), 0) AS call_count,",
            "       COALESCE(SUM(call_failure), 0) AS call_failure",
            "FROM api_key_statistics",
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
            "GROUP BY date",
            "ORDER BY date ASC",
            "</script>"
    })
    List<ApiKeyUsageAggregateEntity> selectTrend(@Param("userId") String userId,
                                                 @Param("orgId") String orgId,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate,
                                                 @Param("apiKeyIds") List<String> apiKeyIds,
                                                 @Param("methodPaths") List<String> methodPaths);

    @Select({
            "<script>",
            "SELECT api_key_id, method_path,",
            "       COALESCE(SUM(call_count), 0) AS call_count,",
            "       COALESCE(SUM(call_failure), 0) AS call_failure,",
            "       COALESCE(SUM(stream_count), 0) AS stream_count,",
            "       COALESCE(SUM(non_stream_count), 0) AS non_stream_count,",
            "       COALESCE(SUM(stream_failure), 0) AS stream_failure,",
            "       COALESCE(SUM(non_stream_failure), 0) AS non_stream_failure,",
            "       COALESCE(SUM(stream_costs), 0) AS stream_costs,",
            "       COALESCE(SUM(non_stream_costs), 0) AS non_stream_costs",
            "FROM api_key_statistics",
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
            "GROUP BY api_key_id, method_path",
            "ORDER BY call_count DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<ApiKeyUsageAggregateEntity> selectGroupedPage(@Param("userId") String userId,
                                                       @Param("orgId") String orgId,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate,
                                                       @Param("apiKeyIds") List<String> apiKeyIds,
                                                       @Param("methodPaths") List<String> methodPaths,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM (",
            "  SELECT api_key_id, method_path",
            "  FROM api_key_statistics",
            "  WHERE user_id = #{userId}",
            "    AND org_id = #{orgId}",
            "    AND date <![CDATA[>=]]> #{startDate}",
            "    AND date <![CDATA[<=]]> #{endDate}",
            "  <if test='apiKeyIds != null and apiKeyIds.size > 0'>",
            "    AND api_key_id IN",
            "    <foreach collection='apiKeyIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "  </if>",
            "  <if test='methodPaths != null and methodPaths.size > 0'>",
            "    AND method_path IN",
            "    <foreach collection='methodPaths' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "  </if>",
            "  GROUP BY api_key_id, method_path",
            ") t",
            "</script>"
    })
    long countGrouped(@Param("userId") String userId,
                      @Param("orgId") String orgId,
                      @Param("startDate") String startDate,
                      @Param("endDate") String endDate,
                      @Param("apiKeyIds") List<String> apiKeyIds,
                      @Param("methodPaths") List<String> methodPaths);

    @Select({
            "SELECT DISTINCT method_path",
            "FROM api_key_statistics",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "ORDER BY method_path ASC"
    })
    List<String> selectMethodPaths(@Param("userId") String userId, @Param("orgId") String orgId);
}
