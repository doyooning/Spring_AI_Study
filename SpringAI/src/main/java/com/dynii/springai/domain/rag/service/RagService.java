package com.dynii.springai.domain.rag.service;

import com.dynii.springai.domain.rag.dto.RagResponse;
import com.dynii.springai.config.RagVectorProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
public class RagService {

    private final RedisVectorStore vectorStore;
    private final ChatModel chatModel;
    private final RagVectorProperties properties;

    public RagService(RedisVectorStore vectorStore, ChatModel chatModel, RagVectorProperties properties) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
        this.properties = properties;
    }

    public RagResponse chat(String question, int topK) {
        int candidates = topK > 0 ? topK : properties.getTopK();
        log.info("🔥 RAG chat() called. question={}", question);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(candidates)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        log.info("🔥 similaritySearch result size={}", documents.size());

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(
                """
                        당신은 고객지원을 대체하는 AI 상담 챗봇입니다.
                        반드시 제공된 문서(Context)에 포함된 정보만을 사용하여 답변하세요.
                        문서에 근거가 없는 질문에는 추측하거나 일반적인 답변을 하지 말고
                        "해당 내용은 현재 제공된 정보로는 안내할 수 없습니다."라고 답변하세요.
                        """
        ));
        messages.add(new UserMessage("Context:\n" + context + "\n\nQuestion: " + question));

        ChatClient chatClient = ChatClient.create(chatModel);
        String answer = chatClient.prompt(new Prompt(messages)).call().content();

        List<String> sources = documents.stream()
                .map(doc -> String.valueOf(doc.getMetadata().getOrDefault("source", "")))
                .toList();

        return new RagResponse(answer, sources);
    }

    public void ingest(List<Document> documents) {
        vectorStore.add(documents);
        log.info("vectorStore added: " + documents);
    }

    public Document createDocument(String content, Map<String, Object> metadata) {
        return new Document(content, metadata);
    }
}
