package com.unicomai.wanwu.service.agent.web;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ApiResponse;
import com.unicomai.wanwu.service.agent.rpc.AgentServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentServiceInfoController {

    @GetMapping("/service-info")
    public ApiResponse<ServiceDescriptor> serviceInfo() {
        return ApiResponse.success(AgentServiceImpl.descriptor());
    }
}
