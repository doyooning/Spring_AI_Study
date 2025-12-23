package com.dynii.springai.domain.rag.service;

import com.dynii.springai.domain.openai.entity.ChatEntity;
import com.dynii.springai.domain.openai.entity.Conversation;
import com.dynii.springai.domain.openai.entity.ConversationStatus;
import com.dynii.springai.domain.openai.repository.ChatRepository;
import com.dynii.springai.domain.rag.dto.ChatResponse;
import com.dynii.springai.domain.rag.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminEscalationService {

    private final ConversationRepository conversationRepository;
    private final ChatRepository chatRepository;
    private final ChatSaveService chatSaveService;
    private final ChatMemoryRepository chatMemoryRepository;

    @Transactional
    public ChatResponse escalate(String question, long conversationId, String userId) {

        String escalateMessage = "현재 상담 내용이 관리자에게 이관되었어요.\n곧 관리자를 통해 답변드릴게요.";

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation not found. id=" + conversationId
                ));

        conversation.setStatus(ConversationStatus.ESCALATED);
        conversationRepository.save(conversation);
        log.info(conversation);

        chatSaveService.saveChat(userId, question, escalateMessage);
        chatSaveService.saveChatMemory(userId, question, chatMemoryRepository);

        return ChatResponse.builder()
                .answer(escalateMessage)
                .escalated(true)
                .build();
    }
}