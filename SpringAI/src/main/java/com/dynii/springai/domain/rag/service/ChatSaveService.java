package com.dynii.springai.domain.rag.service;

import com.dynii.springai.domain.openai.entity.ChatEntity;
import com.dynii.springai.domain.openai.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChatSaveService {

    private final ChatRepository chatRepository;

    // chatentity를 저장
    public void saveChat(String userId, String question, String answer) {

        // 사용자 메시지 저장
        ChatEntity userChat = new ChatEntity();
        userChat.setUserId(userId);
        userChat.setType(MessageType.USER);
        userChat.setContent(question);

        // 챗봇 메시지 저장
        ChatEntity assistantChat = new ChatEntity();
        assistantChat.setUserId(userId);
        assistantChat.setType(MessageType.ASSISTANT);
        assistantChat.setContent(answer);

        chatRepository.saveAll(List.of(userChat, assistantChat));
    }

    // chatmemory에 저장
    public void saveChatMemory(String userId, String text, ChatMemoryRepository chatMemoryRepository) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(userId, new UserMessage(text));
    }
}
