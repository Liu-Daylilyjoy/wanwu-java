package com.unicomai.wanwu.service.assistant.web;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ApiResponse;
import com.unicomai.wanwu.service.assistant.rpc.AssistantServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantServiceInfoController {

    @GetMapping("/service-info")
    public ApiResponse<ServiceDescriptor> serviceInfo() {
        return ApiResponse.success(AssistantServiceImpl.descriptor());
    }
}
