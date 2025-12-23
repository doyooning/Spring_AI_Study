package com.dynii.springai.domain.rag.repository;

import com.dynii.springai.domain.openai.entity.Conversation;
import com.dynii.springai.domain.openai.entity.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // 채팅상태 내림차순
    List<Conversation> findByStatus(ConversationStatus status);

    Optional<Conversation> findTopByUserIdOrderByCreatedAtDesc(String userId);
}
