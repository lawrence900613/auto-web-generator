package com.example.autowebgenerator.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates a RedisChatMemoryStore bean bound to spring.data.redis.* properties.
 * The `ttl` field (in seconds) controls how long conversation history persists in Redis.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

    private String host;
    private int port;
    private String username;
    private String password;
    private long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .ttl(ttl);
        if (password != null && !password.isBlank()) {
            // Redis 6/7 ACL auth: set explicit user to avoid NOAUTH with password-only connections.
            builder.user((username == null || username.isBlank()) ? "default" : username);
            builder.password(password);
        }
        return builder.build();
    }
}
