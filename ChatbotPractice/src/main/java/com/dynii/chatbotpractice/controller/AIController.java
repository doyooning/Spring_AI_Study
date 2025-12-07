package com.dynii.chatbotpractice.controller;

import com.dynii.chatbotpractice.config.Config;
import com.dynii.chatbotpractice.entity.ChatMessage;
import com.dynii.chatbotpractice.repository.ChatMessageRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AIController {
    private final ChatClient chatClient;
    private final ChatMessageRepository chatMessageRepository;

    AIController(ChatClient chatClient, ChatMessageRepository chatMessageRepository) {
        this.chatClient = chatClient;
        this.chatMessageRepository = chatMessageRepository;
    }

    @GetMapping("/ai/chat")
    public Map<String, String> completion(
            @RequestParam(value = "message", defaultValue = "농담 하나 해보거라")
            String message
    ) {
        return Map.of("달건이", this.chatClient.prompt().user(message).call().content());
    }

    @GetMapping("/ai/chat/history")
    public List<ChatMessage> getChatHistory(@RequestParam String userId) {
        return chatMessageRepository.findByUserIdOrderByTimestampAsc(userId);
    }

    @PostMapping("/ai/chat")
    public Map<String, String> sendMessage(
            @RequestParam String userId,
            @RequestBody Map<String, String> request) {
        ChatClient.ChatClientRequestSpec prompt = chatClient.prompt().system(Config.DEFAULT_PROMPT);
        String userInput = request.get("userInput");
        String aiResponse = prompt.user(userInput).call().content();

        chatMessageRepository.save(new ChatMessage(userId, "user", userInput));
        chatMessageRepository.save(new ChatMessage(userId, "assistant", aiResponse));

        return Map.of("role", "assistant", "content", aiResponse);
    }
}
