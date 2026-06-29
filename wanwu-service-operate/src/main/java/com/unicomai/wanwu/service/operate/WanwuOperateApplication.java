package com.unicomai.wanwu.service.operate;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo(scanBasePackages = "com.unicomai.wanwu.service.operate.rpc")
@SpringBootApplication(scanBasePackages = "com.unicomai.wanwu")
public class WanwuOperateApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanwuOperateApplication.class, args);
    }
}
