package com.lcs.beautybackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.lcs.beautybackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class BeautyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeautyBackendApplication.class, args);
    }

}
