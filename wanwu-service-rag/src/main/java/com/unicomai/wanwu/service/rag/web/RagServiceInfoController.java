package com.unicomai.wanwu.service.rag.web;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ApiResponse;
import com.unicomai.wanwu.service.rag.rpc.RagServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class RagServiceInfoController {

    @GetMapping("/service-info")
    public ApiResponse<ServiceDescriptor> serviceInfo() {
        return ApiResponse.success(RagServiceImpl.descriptor());
    }
}
