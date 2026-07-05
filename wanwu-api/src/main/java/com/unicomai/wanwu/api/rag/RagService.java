package com.unicomai.wanwu.api.rag;

import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;

public interface RagService {

    ServiceDescriptor describe();

    Map<String, Object> chatRag(Map<String, Object> request);

    Map<String, Object> createRag(Map<String, Object> request);

    void updateRag(Map<String, Object> request);

    void updateRagConfig(Map<String, Object> request);

    void deleteRag(Map<String, Object> request);

    Map<String, Object> getRagDetail(Map<String, Object> request);

    Map<String, Object> listRag(Map<String, Object> request);

    Map<String, Object> getRagByIds(Map<String, Object> request);

    Map<String, Object> copyRag(Map<String, Object> request);

    void publishRag(Map<String, Object> request);

    void updatePublishRag(Map<String, Object> request);

    Map<String, Object> listPublishRagHistory(Map<String, Object> request);

    void overwriteRagDraft(Map<String, Object> request);

    Map<String, Object> getPublishRagDesc(Map<String, Object> request);

    Map<String, Object> getPublishRagDescBatch(Map<String, Object> request);
}
