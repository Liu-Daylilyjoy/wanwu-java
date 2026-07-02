package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.ModelStatisticEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ModelStatisticMapper extends BaseMapper<ModelStatisticEntity> {

    @Insert({
            "INSERT INTO model_statistics (",
            "  created_at, updated_at, org_id, user_id, model_id, provider, date,",
            "  model, model_type, prompt_tokens, completion_tokens, total_tokens,",
            "  first_token_latency, costs, call_count, stream_count, non_stream_count,",
            "  call_failure, stream_failure, non_stream_failure",
            ") VALUES (",
            "  #{createdAt}, #{updatedAt}, #{orgId}, #{userId}, #{modelId}, #{provider}, #{date},",
            "  #{model}, #{modelType}, #{promptTokens}, #{completionTokens}, #{totalTokens},",
            "  #{firstTokenLatency}, #{costs}, #{callCount}, #{streamCount}, #{nonStreamCount},",
            "  #{callFailure}, #{streamFailure}, #{nonStreamFailure}",
            ") ON DUPLICATE KEY UPDATE",
            "  updated_at = VALUES(updated_at),",
            "  model = VALUES(model),",
            "  model_type = VALUES(model_type),",
            "  prompt_tokens = prompt_tokens + VALUES(prompt_tokens),",
            "  completion_tokens = completion_tokens + VALUES(completion_tokens),",
            "  total_tokens = total_tokens + VALUES(total_tokens),",
            "  first_token_latency = first_token_latency + VALUES(first_token_latency),",
            "  costs = costs + VALUES(costs),",
            "  call_count = call_count + VALUES(call_count),",
            "  stream_count = stream_count + VALUES(stream_count),",
            "  non_stream_count = non_stream_count + VALUES(non_stream_count),",
            "  call_failure = call_failure + VALUES(call_failure),",
            "  stream_failure = stream_failure + VALUES(stream_failure),",
            "  non_stream_failure = non_stream_failure + VALUES(non_stream_failure)"
    })
    int upsertDelta(ModelStatisticEntity entity);

    @Select({
            "<script>",
            "SELECT",
            "  COALESCE(SUM(prompt_tokens), 0) AS prompt_tokens,",
            "  COALESCE(SUM(completion_tokens), 0) AS completion_tokens,",
            "  COALESCE(SUM(total_tokens), 0) AS total_tokens,",
            "  COALESCE(SUM(first_token_latency), 0) AS first_token_latency,",
            "  COALESCE(SUM(costs), 0) AS costs,",
            "  COALESCE(SUM(call_count), 0) AS call_count,",
            "  COALESCE(SUM(stream_count), 0) AS stream_count,",
            "  COALESCE(SUM(non_stream_count), 0) AS non_stream_count,",
            "  COALESCE(SUM(call_failure), 0) AS call_failure,",
            "  COALESCE(SUM(stream_failure), 0) AS stream_failure,",
            "  COALESCE(SUM(non_stream_failure), 0) AS non_stream_failure",
            "FROM model_statistics",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND date <![CDATA[>=]]> #{startDate}",
            "  AND date <![CDATA[<=]]> #{endDate}",
            "<if test='modelType != null and modelType != \"\"'>",
            "  AND model_type = #{modelType}",
            "</if>",
            "<if test='modelIds != null and modelIds.size > 0'>",
            "  AND model_id IN",
            "  <foreach collection='modelIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "</if>",
            "</script>"
    })
    ModelStatisticEntity selectSum(@Param("userId") String userId,
                                   @Param("orgId") String orgId,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate,
                                   @Param("modelIds") List<String> modelIds,
                                   @Param("modelType") String modelType);

    @Select({
            "<script>",
            "SELECT date,",
            "       COALESCE(SUM(call_count), 0) AS call_count,",
            "       COALESCE(SUM(call_failure), 0) AS call_failure,",
            "       COALESCE(SUM(prompt_tokens), 0) AS prompt_tokens,",
            "       COALESCE(SUM(completion_tokens), 0) AS completion_tokens,",
            "       COALESCE(SUM(total_tokens), 0) AS total_tokens",
            "FROM model_statistics",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND date <![CDATA[>=]]> #{startDate}",
            "  AND date <![CDATA[<=]]> #{endDate}",
            "<if test='modelType != null and modelType != \"\"'>",
            "  AND model_type = #{modelType}",
            "</if>",
            "<if test='modelIds != null and modelIds.size > 0'>",
            "  AND model_id IN",
            "  <foreach collection='modelIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "</if>",
            "GROUP BY date",
            "ORDER BY date ASC",
            "</script>"
    })
    List<ModelStatisticEntity> selectTrend(@Param("userId") String userId,
                                           @Param("orgId") String orgId,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("modelIds") List<String> modelIds,
                                           @Param("modelType") String modelType);

    @Select({
            "<script>",
            "SELECT model_id, MIN(model) AS model, MIN(provider) AS provider, MIN(org_id) AS org_id,",
            "       COALESCE(SUM(call_count), 0) AS call_count,",
            "       COALESCE(SUM(call_failure), 0) AS call_failure,",
            "       COALESCE(SUM(prompt_tokens), 0) AS prompt_tokens,",
            "       COALESCE(SUM(completion_tokens), 0) AS completion_tokens,",
            "       COALESCE(SUM(total_tokens), 0) AS total_tokens,",
            "       COALESCE(SUM(first_token_latency), 0) AS first_token_latency,",
            "       COALESCE(SUM(costs), 0) AS costs,",
            "       COALESCE(SUM(stream_count), 0) AS stream_count,",
            "       COALESCE(SUM(non_stream_count), 0) AS non_stream_count,",
            "       COALESCE(SUM(stream_failure), 0) AS stream_failure,",
            "       COALESCE(SUM(non_stream_failure), 0) AS non_stream_failure",
            "FROM model_statistics",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND date <![CDATA[>=]]> #{startDate}",
            "  AND date <![CDATA[<=]]> #{endDate}",
            "<if test='modelType != null and modelType != \"\"'>",
            "  AND model_type = #{modelType}",
            "</if>",
            "<if test='modelIds != null and modelIds.size > 0'>",
            "  AND model_id IN",
            "  <foreach collection='modelIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "</if>",
            "GROUP BY model_id",
            "ORDER BY call_count DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<ModelStatisticEntity> selectGroupedPage(@Param("userId") String userId,
                                                 @Param("orgId") String orgId,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate,
                                                 @Param("modelIds") List<String> modelIds,
                                                 @Param("modelType") String modelType,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM (",
            "  SELECT model_id",
            "  FROM model_statistics",
            "  WHERE user_id = #{userId}",
            "    AND org_id = #{orgId}",
            "    AND date <![CDATA[>=]]> #{startDate}",
            "    AND date <![CDATA[<=]]> #{endDate}",
            "  <if test='modelType != null and modelType != \"\"'>",
            "    AND model_type = #{modelType}",
            "  </if>",
            "  <if test='modelIds != null and modelIds.size > 0'>",
            "    AND model_id IN",
            "    <foreach collection='modelIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>",
            "  </if>",
            "  GROUP BY model_id",
            ") t",
            "</script>"
    })
    long countGrouped(@Param("userId") String userId,
                      @Param("orgId") String orgId,
                      @Param("startDate") String startDate,
                      @Param("endDate") String endDate,
                      @Param("modelIds") List<String> modelIds,
                      @Param("modelType") String modelType);
}
