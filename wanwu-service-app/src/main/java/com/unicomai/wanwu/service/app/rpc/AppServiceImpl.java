package com.unicomai.wanwu.service.app.rpc;

import com.unicomai.wanwu.api.app.AppService;
import com.unicomai.wanwu.api.app.dto.ApplicationListQuery;
import com.unicomai.wanwu.api.app.dto.ApplicationListResult;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collections;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class AppServiceImpl implements AppService {

    @Override
    public ServiceDescriptor describe() {
        return descriptor();
    }

    @Override
    public ApplicationListResult listAssistants(ApplicationListQuery query) {
        return new ApplicationListResult(Collections.emptyList());
    }

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.APP, "App Service", "app");
    }
}
