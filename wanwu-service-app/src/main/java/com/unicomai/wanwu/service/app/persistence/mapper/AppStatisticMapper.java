package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.AppStatisticEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AppStatisticMapper extends BaseMapper<AppStatisticEntity> {

    @Insert({
            "INSERT INTO app_statistics (",
            "  created_at, updated_at, org_id, user_id, app_id, app_type, date,",
            "  call_count, call_failure, stream_count, stream_failure, stream_costs,",
            "  non_stream_count, non_stream_failure, non_stream_costs,",
            "  web_call_count, web_call_failure, openapi_call_count, openapi_call_failure,",
            "  web_url_call_count, web_url_call_failure",
            ") VALUES (",
            "  #{createdAt}, #{updatedAt}, #{orgId}, #{userId}, #{appId}, #{appType}, #{date},",
            "  #{callCount}, #{callFailure}, #{streamCount}, #{streamFailure}, #{streamCosts},",
            "  #{nonStreamCount}, #{nonStreamFailure}, #{nonStreamCosts},",
            "  #{webCallCount}, #{webCallFailure}, #{openapiCallCount}, #{openapiCallFailure},",
            "  #{webUrlCallCount}, #{webUrlCallFailure}",
            ") ON DUPLICATE KEY UPDATE",
            "  updated_at = VALUES(updated_at),",
            "  call_count = call_count + VALUES(call_count),",
            "  call_failure = call_failure + VALUES(call_failure),",
            "  stream_count = stream_count + VALUES(stream_count),",
            "  stream_failure = stream_failure + VALUES(stream_failure),",
            "  stream_costs = stream_costs + VALUES(stream_costs),",
            "  non_stream_count = non_stream_count + VALUES(non_stream_count),",
            "  non_stream_failure = non_stream_failure + VALUES(non_stream_failure),",
            "  non_stream_costs = non_stream_costs + VALUES(non_stream_costs),",
            "  web_call_count = web_call_count + VALUES(web_call_count),",
            "  web_call_failure = web_call_failure + VALUES(web_call_failure),",
            "  openapi_call_count = openapi_call_count + VALUES(openapi_call_count),",
            "  openapi_call_failure = openapi_call_failure + VALUES(openapi_call_failure),",
            "  web_url_call_count = web_url_call_count + VALUES(web_url_call_count),",
            "  web_url_call_failure = web_url_call_failure + VALUES(web_url_call_failure)"
    })
    int upsertDelta(AppStatisticEntity entity);

    @Select({
            "<script>",
            "SELECT",
            "  COALESCE(SUM(call_count), 0) AS call_count,",
            "  COALESCE(SUM(call_failure), 0) AS call_failure,",
            "  COALESCE(SUM(stream_count), 0) AS stream_count,",
            "  COALESCE(SUM(stream_failure), 0) AS stream_failure,",
            "  COALESCE(SUM(stream_costs), 0) AS stream_costs,",
            "  COALESCE(SUM(non_stream_count), 0) AS non_stream_count,",
            "  COALESCE(SUM(non_stream_failure), 0) AS non_stream_failure,",
            "  COALESCE(SUM(non_stream_costs), 0) AS non_stream_costs,",
            "  COALESCE(SUM(web_call_count), 0) AS web_call_count,",
            "  COALESCE(SUM(web_call_failure), 0) AS web_call_failure,",
            "  COALESCE(SUM(openapi_call_count), 0) AS openapi_call_count,",
            "  COALESCE(SUM(openapi_call_failure), 0) AS openapi_call_failure,",
            "  COALESCE(SUM(web_url_call_count), 0) AS web_url_call_count,",
            "  COALESCE(SUM(web_url_call_failure), 0) AS web_url_call_failure",
            "FROM app_statistics",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND date <![CDATA[>=]]> #{startDate}",
            "  AND date <![CDATA[<=]]> #{endDate}",
            "<if test='appType != null and appType != \"\"'>",
            "  AND app_type = #{appType}",
            "</if>",
            "<if test='appIds != null and appIds.size > 0'>",
            "  AND app_id IN",
            "  <foreach collection='appIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "</if>",
            "</script>"
    })
    AppStatisticEntity selectSum(@Param("userId") String userId,
                                 @Param("orgId") String orgId,
                                 @Param("startDate") String startDate,
                                 @Param("endDate") String endDate,
                                 @Param("appIds") List<String> appIds,
                                 @Param("appType") String appType);

    @Select({
            "<script>",
            "SELECT date,",
            "       COALESCE(SUM(call_count), 0) AS call_count,",
            "       COALESCE(SUM(call_failure), 0) AS call_failure,",
            "       COALESCE(SUM(stream_count), 0) AS stream_count,",
            "       COALESCE(SUM(stream_failure), 0) AS stream_failure,",
            "       COALESCE(SUM(stream_costs), 0) AS stream_costs,",
            "       COALESCE(SUM(non_stream_count), 0) AS non_stream_count,",
            "       COALESCE(SUM(non_stream_failure), 0) AS non_stream_failure,",
            "       COALESCE(SUM(non_stream_costs), 0) AS non_stream_costs,",
            "       COALESCE(SUM(web_call_count), 0) AS web_call_count,",
            "       COALESCE(SUM(openapi_call_count), 0) AS openapi_call_count,",
            "       COALESCE(SUM(web_url_call_count), 0) AS web_url_call_count",
            "FROM app_statistics",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND date <![CDATA[>=]]> #{startDate}",
            "  AND date <![CDATA[<=]]> #{endDate}",
            "<if test='appType != null and appType != \"\"'>",
            "  AND app_type = #{appType}",
            "</if>",
            "<if test='appIds != null and appIds.size > 0'>",
            "  AND app_id IN",
            "  <foreach collection='appIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "</if>",
            "GROUP BY date",
            "ORDER BY date ASC",
            "</script>"
    })
    List<AppStatisticEntity> selectTrend(@Param("userId") String userId,
                                         @Param("orgId") String orgId,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("appIds") List<String> appIds,
                                         @Param("appType") String appType);

    @Select({
            "<script>",
            "SELECT app_id, MIN(app_type) AS app_type, MIN(org_id) AS org_id,",
            "       COALESCE(SUM(call_count), 0) AS call_count,",
            "       COALESCE(SUM(call_failure), 0) AS call_failure,",
            "       COALESCE(SUM(stream_count), 0) AS stream_count,",
            "       COALESCE(SUM(stream_failure), 0) AS stream_failure,",
            "       COALESCE(SUM(stream_costs), 0) AS stream_costs,",
            "       COALESCE(SUM(non_stream_count), 0) AS non_stream_count,",
            "       COALESCE(SUM(non_stream_failure), 0) AS non_stream_failure,",
            "       COALESCE(SUM(non_stream_costs), 0) AS non_stream_costs",
            "FROM app_statistics",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND date <![CDATA[>=]]> #{startDate}",
            "  AND date <![CDATA[<=]]> #{endDate}",
            "<if test='appType != null and appType != \"\"'>",
            "  AND app_type = #{appType}",
            "</if>",
            "<if test='appIds != null and appIds.size > 0'>",
            "  AND app_id IN",
            "  <foreach collection='appIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "</if>",
            "GROUP BY app_id",
            "ORDER BY call_count DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<AppStatisticEntity> selectGroupedPage(@Param("userId") String userId,
                                               @Param("orgId") String orgId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate,
                                               @Param("appIds") List<String> appIds,
                                               @Param("appType") String appType,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM (",
            "  SELECT app_id",
            "  FROM app_statistics",
            "  WHERE user_id = #{userId}",
            "    AND org_id = #{orgId}",
            "    AND date <![CDATA[>=]]> #{startDate}",
            "    AND date <![CDATA[<=]]> #{endDate}",
            "  <if test='appType != null and appType != \"\"'>",
            "    AND app_type = #{appType}",
            "  </if>",
            "  <if test='appIds != null and appIds.size > 0'>",
            "    AND app_id IN",
            "    <foreach collection='appIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "  </if>",
            "  GROUP BY app_id",
            ") t",
            "</script>"
    })
    long countGrouped(@Param("userId") String userId,
                      @Param("orgId") String orgId,
                      @Param("startDate") String startDate,
                      @Param("endDate") String endDate,
                      @Param("appIds") List<String> appIds,
                      @Param("appType") String appType);
}
