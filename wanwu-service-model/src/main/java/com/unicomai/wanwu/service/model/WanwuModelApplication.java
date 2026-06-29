package com.unicomai.wanwu.service.model;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo(scanBasePackages = "com.unicomai.wanwu.service.model.rpc")
@SpringBootApplication(scanBasePackages = "com.unicomai.wanwu")
public class WanwuModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanwuModelApplication.class, args);
    }
}
