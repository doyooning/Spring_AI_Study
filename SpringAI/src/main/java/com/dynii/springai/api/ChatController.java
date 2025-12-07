package com.dynii.springai.api;

import com.dynii.springai.domain.openai.dto.CityResponseDTO;
import com.dynii.springai.domain.openai.entity.ChatEntity;
import com.dynii.springai.domain.openai.service.ChatService;
import com.dynii.springai.domain.openai.service.OpenAIService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    private final OpenAIService openAIService;
    private final ChatService chatService;

    public ChatController(OpenAIService openAIService, ChatService chatService) {
        this.openAIService = openAIService;
        this.chatService = chatService;
    }

    // 채팅 페이지 접속
    @GetMapping("/")
    public String chatPage() {
        return "chat";
    }

    // 논 스트림
    @ResponseBody
    @PostMapping("/chat")
    public CityResponseDTO chat(@RequestBody Map<String, String> body) {
        return openAIService.generate(body.get("text"));
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
}
