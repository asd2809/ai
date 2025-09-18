package com.yupi.cbjaicodemother;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.yupi.cbjaicodemother.mapper")
public class CbjAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(CbjAiCodeMotherApplication.class, args);
    }

}
