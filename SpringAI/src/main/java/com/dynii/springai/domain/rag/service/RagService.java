package com.dynii.springai.domain.rag.service;

import com.dynii.springai.domain.rag.dto.RagResponse;
import com.dynii.springai.config.RedisVectorStoreProperties;
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

@Service
public class RagService {

    private final RedisVectorStore vectorStore;
    private final ChatModel chatModel;
    private final RedisVectorStoreProperties properties;

    public RagService(RedisVectorStore vectorStore, ChatModel chatModel, RedisVectorStoreProperties properties) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
        this.properties = properties;
    }

    public RagResponse chat(String question, int topK) {
        int candidates = topK > 0 ? topK : properties.getTopK();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(candidates)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        String context = documents.stream()
                .map(Document::getText)   // 아래 2-2 설명
                .collect(Collectors.joining("\n\n"));

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(
                "You are a helpful assistant that answers based on the provided context. " +
                        "If the answer is not in the context, say you do not know."
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
    }

    public Document createDocument(String content, Map<String, Object> metadata) {
        return new Document(content, metadata);
    }
}
