package com.dynii.springai.domain.rag.service;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RagIngestor {

    private final RagService ragService;

    public RagIngestor(RagService ragService) {
        this.ragService = ragService;
    }

    @PostConstruct
    public void seedSampleDocuments() {
        List<Document> documents = List.of(
                ragService.createDocument("스프링 AI는 스프링 부트 애플리케이션에서 AI 모델을 쉽게 사용할 수 있게 해주는 프로젝트입니다.", Map.of("source", "spring-ai-intro")),
                ragService.createDocument("Redis는 인메모리 데이터 스토어로 빠른 읽기와 쓰기 성능을 제공합니다.", Map.of("source", "redis-overview")),
                ragService.createDocument("벡터 검색은 문서의 의미적 유사성을 기반으로 검색하는 방법입니다.", Map.of("source", "vector-search"))
        );
        ragService.ingest(documents);
    }
}
