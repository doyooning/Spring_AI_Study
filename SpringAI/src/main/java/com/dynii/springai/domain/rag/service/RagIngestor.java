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
                ragService.createDocument("DESKIT은 개인의 작업 공간을 꾸미는 '데스크테리어' 상품에 특화된 라이브커머스 기반 쇼핑 플랫폼입니다.", Map.of("source", "deskit-intro")),
                ragService.createDocument("DESKIT의 개발자는 고하원, 김도윤, 박용헌, 주장우입니다.", Map.of("source", "deskit-developers"))
        );
        ragService.ingest(documents);
    }
}
