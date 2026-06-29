package com.unicomai.wanwu.common.web.health;

import com.unicomai.wanwu.common.core.model.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal")
public class HealthController {

    private final String applicationName;

    public HealthController(@Value("${spring.application.name:wanwu-service}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> payload = new LinkedHashMap<String, String>();
        payload.put("service", applicationName);
        payload.put("status", "UP");
        return ApiResponse.success(payload);
    }
}
