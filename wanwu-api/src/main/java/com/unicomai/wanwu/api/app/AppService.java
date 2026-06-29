package com.unicomai.wanwu.api.app;

import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.common.ServiceDescriptor;

public interface AppService {

    ServiceDescriptor describe();

    ApplicationListResult listAssistants(ApplicationListQuery query);
}
