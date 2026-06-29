package com.unicomai.wanwu.service.bff;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo(scanBasePackages = "com.unicomai.wanwu.service.bff.rpc")
@SpringBootApplication(scanBasePackages = "com.unicomai.wanwu")
public class WanwuBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanwuBffApplication.class, args);
    }
}
