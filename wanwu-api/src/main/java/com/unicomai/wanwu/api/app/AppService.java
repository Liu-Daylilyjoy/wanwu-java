package com.unicomai.wanwu.api.app;

import com.unicomai.wanwu.api.app.dto.AssistantCreateCommand;
import com.unicomai.wanwu.api.app.dto.AssistantCreateResult;
import com.unicomai.wanwu.api.app.dto.AssistantDetailQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.common.ServiceDescriptor;

import java.util.Map;

public interface AppService {

    ServiceDescriptor describe();

    AssistantCreateResult createAssistant(AssistantCreateCommand command);

    ApplicationListResult listAssistants(ApplicationListQuery query);

    Map<String, Object> getAssistantDraft(AssistantDetailQuery query);
}
