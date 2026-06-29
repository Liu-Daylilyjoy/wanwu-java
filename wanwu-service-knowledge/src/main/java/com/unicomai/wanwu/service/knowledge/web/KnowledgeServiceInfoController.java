package com.unicomai.wanwu.service.knowledge.web;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ApiResponse;
import com.unicomai.wanwu.service.knowledge.rpc.KnowledgeServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeServiceInfoController {

    @GetMapping("/service-info")
    public ApiResponse<ServiceDescriptor> serviceInfo() {
        return ApiResponse.success(KnowledgeServiceImpl.descriptor());
    }
}
