package com.dynii.springai.domain.rag.entity;

public record RouteDecision(ChatRoute route, int score, String reason) {
}
