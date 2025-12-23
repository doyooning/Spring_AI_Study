package com.dynii.springai.api.admin;

import com.dynii.springai.domain.openai.entity.Conversation;
import com.dynii.springai.domain.openai.entity.ConversationStatus;
import com.dynii.springai.domain.rag.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/conversations")
public class AdminConversationController {

    private final ConversationRepository conversationRepository;

    // 이관된 채팅 목록 조회
    @GetMapping("/escalated")
    public List<Conversation> getEscalatedConversations() {
        return conversationRepository.findByStatus(ConversationStatus.ESCALATED);
    }

    @PostMapping("/{conversationId}/start")
    public void startConversation(@PathVariable Long conversationId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Conversation not found: " + conversationId)
                );

        conversation.setStatus(ConversationStatus.ADMIN_ACTIVE);
        conversationRepository.save(conversation);
    }

}

