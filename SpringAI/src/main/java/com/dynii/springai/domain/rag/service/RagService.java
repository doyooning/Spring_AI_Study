package com.dynii.springai.domain.rag.service;

import com.dynii.springai.domain.rag.dto.ChatResponse;
import com.dynii.springai.config.RagVectorProperties;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class RagService {

    private final RedisVectorStore vectorStore;
    private final ChatModel chatModel;
    private final RagVectorProperties properties;
    private final AdminEscalationService adminEscalationService;

    // 관리자 이관 트리거
    private static final String ESCALATION_TRIGGER = "관리자 연결";

    public ChatResponse chat(String question, int topK) {
        // 이관 여부를 담는 escalated
        boolean escalated = false;
        long conversationId = 1L;

        // 트리거가 포함된 질문이면 이관
        if (question != null && question.contains(ESCALATION_TRIGGER)) {

            /* 여기에 이관 조치할 때 수행할 로직 추가 */
            return adminEscalationService.escalate(conversationId);
        }

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
                        
                        문서에 질문과 정확히 일치하는 내용이 없더라도,
                        관련된 정보가 문서에 일부라도 포함되어 있다면
                        그 범위 내에서만 요약하여 안내하세요.
                        
                        단, 문서에 전혀 근거가 없는 내용은
                        추측하거나 일반적인 답변을 하지 마세요.
                        
                        다음과 같은 경우에는 직접 답변하지 말고,
                        관리자 상담이 필요하다는 안내를 하세요.
                        
                        1. 제공된 문서(Context)에 근거가 없는 질문인 경우
                        2. 사용자의 개인적인 상황, 주문 내역, 결제 정보, 계정 상태 등
                           개인 정보 또는 개인별 처리가 필요한 질문인 경우
                        3. 정책 문서에 없는 예외 처리, 임의 판단, 특수 요청을 요구하는 경우
                        4. 문서 내용만으로 정확하고 책임 있는 답변을 제공할 수 없다고 판단되는 경우
                        5. "관리자 문의", "사람이랑 상담", "직접 문의하고 싶다" 등 고객이 명시적으로 관리자 상담을 요청하는 경우
                        
                        위 조건에 해당하는 경우에는
                        반드시 아래 문장 중 하나의 형태로만 응답하세요.
                        
                        - "해당 내용은 현재 제공된 정보로는 안내드릴 수 없어요. "관리자 연결"을 입력하시면 관리자에게 문의가 접수돼요."
                        - "개인 정보 또는 개별 확인이 필요한 내용으로, 관리자 상담을 통해 안내가 가능해요. "관리자 연결"을 입력하시면 관리자에게 문의가 접수돼요."
                        - "요청하신 내용은 관리자 확인이 필요하여 상담 연결이 필요해요. "관리자 연결"을 입력하시면 관리자에게 문의가 접수돼요."
                        - "관리자와 직접 상담을 원하시는 것 같아요. "관리자 연결"을 입력하시면 관리자에게 문의가 접수돼요."
                        
                        절대 위 문구 외의 임의의 답변을 생성하지 마세요.
                        
                        만약 고객이 "관리자 연결"이라고 입력한 경우에는
                        반드시 아래 문장 중 하나의 형태로만 응답하세요.
                        
                        - "현재 상담 내용이 관리자에게 이관되었어요. 곧 관리자를 통해 답변드릴게요."
                        - "고객님의 문의가 관리자에게 이관되었어요. 곧 관리자를 통해 답변드릴게요."
                        
                        절대 위 문구 외의 임의의 답변을 생성하지 마세요.
                        """
        ));
        messages.add(new UserMessage("Context:\n" + context + "\n\nQuestion: " + question));

        ChatClient chatClient = ChatClient.create(chatModel);
        String answer = chatClient.prompt(new Prompt(messages)).call().content();

        List<String> sources = documents.stream()
                .map(doc -> String.valueOf(doc.getMetadata().getOrDefault("source", "")))
                .toList();

        log.info("Chatbot sources: {}", sources);
        log.info("Chatbot escalated: {}", escalated);
        return new ChatResponse(answer, sources, escalated);
    }

    public void ingest(List<Document> documents) {
        vectorStore.add(documents);
        log.info("vectorStore added: " + documents);
    }

    public Document createDocument(String content, Map<String, Object> metadata) {
        return new Document(content, metadata);
    }
}
