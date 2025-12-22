package com.dynii.springai.domain.rag.repository;

import com.dynii.springai.domain.openai.entity.Conversation;
import com.dynii.springai.domain.openai.entity.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Override
    Optional<Conversation> findById(Long conversationId);

    void save(long conversationId);

    // 채팅상태 내림차순
    List<Conversation> findByStatus(ConversationStatus status);

    //
    Optional<Conversation> findTopByUserIdOrderByCreatedAtDesc(String userId);
}
