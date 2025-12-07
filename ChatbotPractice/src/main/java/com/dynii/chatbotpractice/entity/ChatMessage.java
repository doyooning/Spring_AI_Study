package com.dynii.chatbotpractice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(nullable = false)
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public ChatMessage() {}
    public ChatMessage(String userId, String role, String content) {
        this.userId = userId;
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
}
