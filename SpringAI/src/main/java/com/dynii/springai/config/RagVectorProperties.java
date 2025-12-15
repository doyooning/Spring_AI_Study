package com.dynii.springai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "redis.vector")
public class RagVectorProperties {

    private String host = "localhost";
    private int port = 6379;
    private String password;
    private String indexName = "chat-index";
    private String prefix = "doc:";
    private int topK = 4;

}
