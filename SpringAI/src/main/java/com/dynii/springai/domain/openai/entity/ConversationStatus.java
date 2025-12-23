package com.dynii.springai.domain.openai.entity;

public enum ConversationStatus {
    BOT_ACTIVE,        // 챗봇 응답 중
    ESCALATED,         // 관리자 이관 요청됨 (대기)
    ADMIN_ACTIVE,      // 관리자 상담 중
    CLOSED;             // 상담 종료
}
