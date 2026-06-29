package com.unicomai.wanwu.service.knowledge;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo(scanBasePackages = "com.unicomai.wanwu.service.knowledge.rpc")
@SpringBootApplication(scanBasePackages = "com.unicomai.wanwu")
public class WanwuKnowledgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanwuKnowledgeApplication.class, args);
    }
}
