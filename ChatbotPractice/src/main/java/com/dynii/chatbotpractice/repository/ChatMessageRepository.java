package com.dynii.chatbotpractice.repository;

import com.dynii.chatbotpractice.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findByUserIdOrderByTimestampAsc(String userId);
}
