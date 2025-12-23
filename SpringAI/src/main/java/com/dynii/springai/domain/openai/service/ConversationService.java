package com.dynii.springai.domain.openai.service;

import com.dynii.springai.domain.openai.entity.Conversation;
import com.dynii.springai.domain.openai.entity.ConversationStatus;
import com.dynii.springai.domain.rag.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    @Transactional(readOnly = true)
    public Conversation getLatestConversation(String userId) {
        return conversationRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalStateException("Conversation not found for userId=" + userId));
    }

    @Transactional(readOnly = true)
    public Optional<Conversation> findLatestConversation(String userId) {
        return conversationRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
    }

}

