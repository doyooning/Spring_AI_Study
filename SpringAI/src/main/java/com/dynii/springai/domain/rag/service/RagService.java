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
                        
                        반드시 제공된 문서(Context)에 포함된 정보만을 근거로 답변해야 합니다.
                        문서에 명시되지 않은 내용에 대해서는 추측하거나 일반적인 안내를 하지 마세요.
                        
                        다음과 같은 경우에는 직접 답변하지 말고,
                        관리자 상담이 필요하다는 안내를 하세요.
                        
                        1. 제공된 문서(Context)에 근거가 없는 질문인 경우
                        2. 사용자의 개인적인 상황, 주문 내역, 결제 정보, 계정 상태 등
                           개인 정보 또는 개인별 처리가 필요한 질문인 경우
                        3. 정책 문서에 없는 예외 처리, 임의 판단, 특수 요청을 요구하는 경우
                        4. "관리자 연결", "사람이랑 상담", "직접 문의하고 싶다" 등
                           명시적으로 관리자 상담을 요청하는 경우
                        5. 문서 내용만으로 정확하고 책임 있는 답변을 제공할 수 없다고 판단되는 경우
                        
                        위 조건에 해당하는 경우에는
                        반드시 아래 문장 중 하나의 형태로만 응답하세요.
                        
                        - "해당 내용은 현재 제공된 정보로는 안내할 수 없습니다. 관리자에게 문의해 주세요."
                        - "개인 정보 또는 개별 확인이 필요한 내용으로, 관리자 상담을 통해 안내가 가능합니다."
                        - "요청하신 내용은 관리자 확인이 필요하여 상담 연결이 필요합니다."
                        
                        절대 위 문구 외의 임의의 답변을 생성하지 마세요.
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
