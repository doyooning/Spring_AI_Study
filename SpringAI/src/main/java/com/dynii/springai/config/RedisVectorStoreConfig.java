package com.dynii.springai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(RagVectorProperties.class)
public class RedisVectorStoreConfig {

    @Bean
    public JedisPooled jedisPooled(RagVectorProperties properties) {

        if (properties.getPassword() != null && !properties.getPassword().isEmpty()) {
            String uri = String.format(
                    "redis://:%s@%s:%d",
                    properties.getPassword(),
                    properties.getHost(),
                    properties.getPort()
            );
            return new JedisPooled(URI.create(uri));
        }

        return new JedisPooled(
                properties.getHost(),
                properties.getPort()
        );
    }

    @Bean
    public RedisVectorStore redisVectorStore(
            JedisPooled jedisPooled,
            EmbeddingModel embeddingModel,
            RagVectorProperties properties
    ) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(properties.getIndexName())
                .prefix(properties.getPrefix())
                .initializeSchema(true)
                .build();
    }

}
