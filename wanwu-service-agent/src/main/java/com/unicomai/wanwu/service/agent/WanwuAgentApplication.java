package com.unicomai.wanwu.service.agent;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo(scanBasePackages = "com.unicomai.wanwu.service.agent.rpc")
@SpringBootApplication(scanBasePackages = "com.unicomai.wanwu")
public class WanwuAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanwuAgentApplication.class, args);
    }
}
