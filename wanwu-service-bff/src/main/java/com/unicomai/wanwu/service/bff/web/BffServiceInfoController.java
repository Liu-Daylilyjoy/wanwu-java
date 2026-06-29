package com.unicomai.wanwu.service.bff.web;

import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.common.core.model.ApiResponse;
import com.unicomai.wanwu.service.bff.rpc.BffFacadeImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bff")
public class BffServiceInfoController {

    @GetMapping("/service-info")
    public ApiResponse<ServiceDescriptor> serviceInfo() {
        return ApiResponse.success(BffFacadeImpl.descriptor());
    }
}
