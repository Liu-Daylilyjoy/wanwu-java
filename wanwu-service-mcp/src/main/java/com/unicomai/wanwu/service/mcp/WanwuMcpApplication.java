package com.unicomai.wanwu.service.mcp;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo(scanBasePackages = "com.unicomai.wanwu.service.mcp.rpc")
@SpringBootApplication(scanBasePackages = "com.unicomai.wanwu")
public class WanwuMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanwuMcpApplication.class, args);
    }
}
