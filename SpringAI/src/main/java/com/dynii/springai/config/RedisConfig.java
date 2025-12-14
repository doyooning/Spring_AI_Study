package com.dynii.springai.config;

import com.redis.lettucemod.RedisModulesClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPooled;

@Configuration
@EnableConfigurationProperties(RedisVectorStoreProperties.class)
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisStandaloneConfiguration standaloneConfiguration) {
        return new LettuceConnectionFactory(standaloneConfiguration);
    }

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration(RedisVectorStoreProperties properties) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(properties.getHost());
        configuration.setPort(properties.getPort());
        configuration.setPassword(properties.getPassword());
        return configuration;
    }

    @Bean(destroyMethod = "shutdown")
    public RedisModulesClient redisModulesClient(RedisVectorStoreProperties properties) {
        String uri = String.format("redis://%s%s:%d", properties.getPassword() != null && !properties.getPassword().isEmpty() ? properties.getPassword() + "@" : "", properties.getHost(), properties.getPort());
        return RedisModulesClient.create(uri);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisVectorStore redisVectorStore(
            JedisPooled jedisPooled,
            EmbeddingModel embeddingModel,
            RagProperties properties
    ) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(properties.getIndexName())
                .prefix(properties.getPrefix())
                .build();
    }
}
