package com.yupi.cbjaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yupi.cbjaicodemother.mapper")
public class CbjAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(CbjAiCodeMotherApplication.class, args);
    }

}
