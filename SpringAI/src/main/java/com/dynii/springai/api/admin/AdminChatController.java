package com.dynii.springai.api.admin;

import com.dynii.springai.domain.openai.entity.Conversation;
import com.dynii.springai.domain.openai.entity.ConversationStatus;
import com.dynii.springai.domain.rag.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Log4j2
@Controller
@RequiredArgsConstructor
public class AdminChatController {

    private final ConversationRepository conversationRepository;

    @GetMapping("/admin/chats/escalated")
    public List<Conversation> getEscalatedChats() {
        return conversationRepository.findByStatus(ConversationStatus.ESCALATED);
    }

    @PostMapping("/admin/chats/{conversationId}/start")
    public void startAdminChat(@PathVariable Long conversationId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation not found. id=" + conversationId
                ));

        conversation.setStatus(ConversationStatus.ESCALATED);
        conversationRepository.save(conversation);
        log.info(conversation);
    }
}
