package com.dynii.springai.domain.rag.service;

import com.dynii.springai.domain.openai.entity.Conversation;
import com.dynii.springai.domain.openai.entity.ConversationStatus;
import com.dynii.springai.domain.rag.dto.ChatResponse;
import com.dynii.springai.domain.rag.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminEscalationService {

    private final ConversationRepository conversationRepository;

    public ChatResponse escalate(long conversationId) {

        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        conversation.ifPresent(c -> {
            c.setStatus(ConversationStatus.ESCALATED);
        });

        conversationRepository.save(conversationId);

        return ChatResponse.builder()
                .answer("현재 상담 내용이 관리자에게 이관되었습니다.\n곧 관리자를 통해 답변드리겠습니다.")
                .escalated(true)
                .build();
    }
}