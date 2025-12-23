package com.dynii.springai.domain.rag.service;

import com.dynii.springai.domain.openai.entity.Conversation;
import com.dynii.springai.domain.openai.entity.ConversationStatus;
import com.dynii.springai.domain.rag.dto.ChatResponse;
import com.dynii.springai.domain.rag.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminEscalationService {

    private final ConversationRepository conversationRepository;

    public ChatResponse escalate(long conversationId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation not found. id=" + conversationId
                ));

        conversation.setStatus(ConversationStatus.ESCALATED);
        conversationRepository.save(conversation);
        log.info(conversation);

        return ChatResponse.builder()
                .answer("현재 상담 내용이 관리자에게 이관되었어요.\n곧 관리자를 통해 답변드릴게요.")
                .escalated(true)
                .build();
    }
}