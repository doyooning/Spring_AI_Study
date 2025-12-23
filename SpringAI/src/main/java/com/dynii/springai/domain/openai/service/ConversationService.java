package com.dynii.springai.domain.openai.service;

import com.dynii.springai.domain.openai.entity.Conversation;
import com.dynii.springai.domain.openai.entity.ConversationStatus;
import com.dynii.springai.domain.rag.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    @Transactional
    public Conversation getOrCreateActiveConversation(String userId) {

        return conversationRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .filter(c -> c.getStatus() == ConversationStatus.BOT_ACTIVE)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUserId(userId);
                    c.setStatus(ConversationStatus.BOT_ACTIVE);
                    return conversationRepository.save(c);
                });
    }
}

