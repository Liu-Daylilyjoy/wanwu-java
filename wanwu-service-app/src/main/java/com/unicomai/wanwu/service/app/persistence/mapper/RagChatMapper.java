package com.unicomai.wanwu.service.app.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicomai.wanwu.service.app.persistence.entity.RagChatEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RagChatMapper extends BaseMapper<RagChatEntity> {

    @Select({
            "SELECT id, created_at, updated_at, user_id, org_id, rag_id, chat_id, draft,",
            "       question, response, history_json, file_info_json, search_list_json, qa_search_list_json",
            "FROM rag_chat_records",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND rag_id = #{ragId}",
            "ORDER BY created_at DESC, id DESC",
            "LIMIT #{limit}"
    })
    List<RagChatEntity> selectByRag(@Param("userId") String userId,
                                    @Param("orgId") String orgId,
                                    @Param("ragId") String ragId,
                                    @Param("limit") int limit);

    @Delete({
            "DELETE FROM rag_chat_records",
            "WHERE user_id = #{userId}",
            "  AND org_id = #{orgId}",
            "  AND rag_id = #{ragId}"
    })
    int deleteByRag(@Param("userId") String userId,
                    @Param("orgId") String orgId,
                    @Param("ragId") String ragId);
}
