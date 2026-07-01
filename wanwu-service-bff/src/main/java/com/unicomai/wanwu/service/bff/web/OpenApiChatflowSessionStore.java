package com.unicomai.wanwu.service.bff.web;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OpenApiChatflowSessionStore {

    private final Map<String, ConversationState> conversations = new LinkedHashMap<>();
    private final AtomicLong messageSequence = new AtomicLong(100000);

    public synchronized Map<String, Object> create(String userId, String orgId, String appId, String conversationName) {
        ConversationState conversation = new ConversationState();
        conversation.userId = userId;
        conversation.orgId = orgId;
        conversation.appId = defaultIfBlank(appId, "chatflow");
        conversation.conversationId = "chatflow-conversation-" + compactId();
        conversation.conversationName = defaultIfBlank(conversationName, "New conversation");
        conversations.put(key(userId, orgId, conversation.appId, conversation.conversationId), conversation);
        return conversationInfo(conversation);
    }

    public synchronized Map<String, Object> list(String userId, String orgId, String appId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ConversationState conversation : conversations.values()) {
            if (matches(conversation, userId, orgId, appId)) {
                rows.add(conversationInfo(conversation));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("conversations", rows);
        result.put("list", rows);
        result.put("total", rows.size());
        return result;
    }

    public synchronized Map<String, Object> messages(String userId, String orgId, String appId,
                                                     String conversationId, int limit) {
        ConversationState conversation = find(userId, orgId, appId, conversationId);
        List<Map<String, Object>> rows = new ArrayList<>();
        if (conversation != null) {
            int from = limit <= 0 || conversation.messages.size() <= limit
                    ? 0 : conversation.messages.size() - limit;
            for (int i = from; i < conversation.messages.size(); i++) {
                rows.add(new LinkedHashMap<>(conversation.messages.get(i)));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", rows);
        result.put("has_more", false);
        result.put("first_id", rows.isEmpty() ? 0 : rows.get(0).get("id"));
        result.put("last_id", rows.isEmpty() ? 0 : rows.get(rows.size() - 1).get("id"));
        return result;
    }

    public synchronized Map<String, Object> chat(String userId, String orgId, String appId,
                                                 String conversationId, String query, Map<String, Object> parameters) {
        ConversationState conversation = find(userId, orgId, appId, conversationId);
        if (conversation == null) {
            conversation = new ConversationState();
            conversation.userId = userId;
            conversation.orgId = orgId;
            conversation.appId = defaultIfBlank(appId, "chatflow");
            conversation.conversationId = defaultIfBlank(conversationId, "chatflow-conversation-" + compactId());
            conversation.conversationName = "New conversation";
            conversations.put(key(userId, orgId, conversation.appId, conversation.conversationId), conversation);
        }

        String safeQuery = defaultIfBlank(query, "");
        String response = "Chatflow response: " + safeQuery;
        conversation.messages.add(message(conversation, "user", safeQuery, parameters));
        conversation.messages.add(message(conversation, "assistant", response, Collections.<String, Object>emptyMap()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("conversation_id", conversation.conversationId);
        result.put("response", response);
        result.put("finish", 1);
        return result;
    }

    public synchronized void delete(String userId, String orgId, String appId, String conversationId) {
        conversations.remove(key(userId, orgId, defaultIfBlank(appId, "chatflow"), conversationId));
    }

    private ConversationState find(String userId, String orgId, String appId, String conversationId) {
        return conversations.get(key(userId, orgId, defaultIfBlank(appId, "chatflow"), conversationId));
    }

    private boolean matches(ConversationState conversation, String userId, String orgId, String appId) {
        return conversation.userId.equals(userId)
                && conversation.orgId.equals(orgId)
                && conversation.appId.equals(defaultIfBlank(appId, "chatflow"));
    }

    private Map<String, Object> conversationInfo(ConversationState conversation) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("conversation_id", conversation.conversationId);
        data.put("conversationId", conversation.conversationId);
        data.put("conversation_name", conversation.conversationName);
        data.put("conversationName", conversation.conversationName);
        data.put("uuid", conversation.appId);
        return data;
    }

    private Map<String, Object> message(ConversationState conversation, String role, String content,
                                        Map<String, Object> parameters) {
        long id = messageSequence.incrementAndGet();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", String.valueOf(id));
        data.put("bot_id", conversation.appId);
        data.put("role", role);
        data.put("content", content);
        data.put("conversation_id", conversation.conversationId);
        data.put("meta_data", parameters == null ? Collections.emptyMap() : parameters);
        data.put("created_at", 0);
        data.put("updated_at", 0);
        data.put("chat_id", String.valueOf(id));
        data.put("content_type", "text");
        data.put("type", "answer");
        data.put("section_id", "");
        data.put("reasoning_content", "");
        return data;
    }

    private String key(String userId, String orgId, String appId, String conversationId) {
        return defaultIfBlank(userId, "") + "|" + defaultIfBlank(orgId, "") + "|"
                + defaultIfBlank(appId, "chatflow") + "|" + defaultIfBlank(conversationId, "");
    }

    private String compactId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static class ConversationState {
        private String userId;
        private String orgId;
        private String appId;
        private String conversationId;
        private String conversationName;
        private final List<Map<String, Object>> messages = new ArrayList<>();
    }
}
