package com.example.autowebgenerator;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.autowebgenerator.mapper")
public class AutoWebGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoWebGeneratorApplication.class, args);
    }

}
