package com.dynii.springai.domain.rag.service;

import com.dynii.springai.domain.rag.entity.ChatRoute;
import com.dynii.springai.domain.rag.entity.RouteDecision;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Map;

@Log4j2
@Service
public class ChatRoutingService {

    // RAG를 강하게 시사하는 키워드
    private static final Map<String, Integer> RAG_KEYWORDS = Map.ofEntries(
            Map.entry("관리자", 3),
            Map.entry("환불", 3),
            Map.entry("취소", 3),
            Map.entry("반품", 3),
            Map.entry("약관", 3),
            Map.entry("정책", 3),
            Map.entry("수수료", 3),
            Map.entry("가입", 2),
            Map.entry("승인", 2),
            Map.entry("판매자", 2),
            Map.entry("결제", 2),
            Map.entry("정산", 2),
            Map.entry("배송", 2),
            Map.entry("상담", 1),
            Map.entry("문의", 1),
            Map.entry("공지", 1),
            Map.entry("계정", 1)
    );

    // RAG로 보내도 “문서 근거 없이 추측”하게 만들 위험이 큰 질문 패턴
    // (예: 너무 짧거나 대명사만 있는 경우)
    private static final int MIN_LEN_FOR_RAG = 6;

    // 점수 임계치: 이 이상이면 RAG로 보냄
    private static final int RAG_THRESHOLD = 2;

    public RouteDecision decide(String question) {
        String q = normalize(question);
        log.info(q);

        if (q == null || q.isBlank()) {
            log.info("q is blank");
            return new RouteDecision(ChatRoute.GENERAL, 0, "empty");
        }

        // 너무 짧으면 RAG로 보내봤자 검색 품질이 낮아서 일반 챗(재질문 유도)이 더 나음
        if (q.length() < MIN_LEN_FOR_RAG) {
            log.info("q is too short");
            return new RouteDecision(ChatRoute.GENERAL, 0, "too_short");
        }

        int score = 0;
        for (var e : RAG_KEYWORDS.entrySet()) {
            log.info("entrySet : " + e.getKey());
            if (q.contains(e.getKey())) score += e.getValue();
        }

        // 질문 형태가 규칙/정책/가능여부 확인이면 RAG 가산점
        if (looksLikePolicyQuestion(q)) score += 1;

        ChatRoute route = (score >= RAG_THRESHOLD) ? ChatRoute.RAG : ChatRoute.GENERAL;
        log.info(route);
        return new RouteDecision(route, score, "keyword_score");
    }

    private boolean looksLikePolicyQuestion(String q) {
        return q.contains("가능") || q.contains("되나요") || q.contains("기준") || q.contains("조건");
    }

    private String normalize(String q) {
        if (q == null) return null;
        return q.trim();
    }
}

