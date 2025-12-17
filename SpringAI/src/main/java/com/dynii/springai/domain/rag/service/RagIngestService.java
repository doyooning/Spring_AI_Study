package com.dynii.springai.domain.rag.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class RagIngestService {

    private final RedisVectorStore vectorStore;
    public RagIngestService(RedisVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingest(MultipartFile file) {
        try {
            Resource resource = new InputStreamResource(file.getInputStream());
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<Document> documents = reader.get();
            vectorStore.add(documents);
            log.info("📄 RAG 문서 업로드: {}", file.getOriginalFilename());
            log.info("📄 생성된 Document 수: {}", documents.size());

        } catch (IOException e) {
            throw new RuntimeException("문서 임베딩 실패", e);
        }
    }

    public void ingest(Resource resource) {
        DocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();

        String source = resource.getFilename();
        List<Document> enrichedDocs = documents.stream()
                .map(doc -> new Document(
                        doc.getText(),
                        Map.of(
                                "source", source
                        )
                ))
                .toList();
        vectorStore.add(enrichedDocs);
        log.info("📄 RAG 문서 업로드: {}", resource.getFilename());
    }

}
