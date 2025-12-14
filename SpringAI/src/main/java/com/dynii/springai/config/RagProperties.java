package com.dynii.springai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rag")
public class RagProperties {
    private String indexName = "chatbot-index";
    private String prefix = "chatbot:";
    private int topK = 5;
}
