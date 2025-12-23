package com.dynii.springai.api;

import com.dynii.springai.domain.openai.entity.ChatEntity;
import com.dynii.springai.domain.openai.entity.Conversation;
import com.dynii.springai.domain.openai.service.ChatService;
import com.dynii.springai.domain.openai.service.ConversationService;
import com.dynii.springai.domain.openai.service.OpenAIService;
import com.dynii.springai.domain.rag.dto.ChatRequest;
import com.dynii.springai.domain.rag.dto.ChatResponse;
import com.dynii.springai.domain.rag.entity.RouteDecision;
import com.dynii.springai.domain.rag.service.ChatRoutingService;
import com.dynii.springai.domain.rag.service.RagIngestService;
import com.dynii.springai.domain.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final RagIngestService ragIngestService;
    private final OpenAIService openAIService;
    private final ChatService chatService;
    private final RagService ragService;
    private final ChatRoutingService chatRoutingService;
    private final ConversationService conversationService;

    // 채팅 페이지 접속
    @GetMapping("/")
    public String chatPage() {
        return "chat";
    }

    // 논 스트림
    @ResponseBody
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String question = request.getQuestion();
        String userId = "dynii1923";

        // 현재 진행 중인 대화 조회 or 생성
        Conversation conversation = conversationService.getOrCreateActiveConversation(userId);

        if (question == null || question.isBlank()) {
            log.warn("Empty question received: {}", request);
            return null;
        }

        RouteDecision decision = chatRoutingService.decide(question);
        log.info("route={} score={} reason={} q={}",
                decision.route(), decision.score(), decision.reason(), question);


        switch (decision.route()) {
            // RAG로 판단
            case RAG -> {
                return ragService.chat(question, 4);
            }
            // LLM 채팅으로 판단
            case GENERAL -> {
                return openAIService.generate(request.getQuestion());
            }
        }
        return null;
    }

    // 스트림
    @ResponseBody
    @PostMapping("/chat/stream")
    public Flux<String> streamChat(@RequestBody Map<String, String> body) {
        return openAIService.generateStream(body.get("text"));
    }

    @ResponseBody
    @PostMapping("/chat/history/{userid}")
    public List<ChatEntity> getChatHistory(@PathVariable("userid") String userId) {
        return chatService.readAllChats(userId);
    }

    @PostMapping("/admin/rag/upload")
    public ResponseEntity<?> uploadRagDocument(
            @RequestParam("file") MultipartFile file
//            @RequestParam("category") String category // POLICY, FAQ, NOTICE
    ) {
        ragIngestService.ingest(file);
        return ResponseEntity.ok("업로드 완료");
    }

    @ResponseBody
    @GetMapping("/chat/status/{userId}")
    public Map<String, String> getStatus(@PathVariable String userId) {

        return conversationService.findLatestConversation(userId)
                .map(c -> Map.of("status", c.getStatus().name()))
                .orElse(Map.of("status", "BOT_ACTIVE"));
    }


}
