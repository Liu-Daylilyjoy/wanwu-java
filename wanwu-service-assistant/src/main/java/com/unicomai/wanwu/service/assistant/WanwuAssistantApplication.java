package com.unicomai.wanwu.service.assistant;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo(scanBasePackages = "com.unicomai.wanwu.service.assistant.rpc")
@SpringBootApplication(scanBasePackages = "com.unicomai.wanwu")
public class WanwuAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanwuAssistantApplication.class, args);
    }
}
