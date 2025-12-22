package com.dynii.springai.domain.rag.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatResponse {
    private String answer;
    private List<String> sources;
    private boolean escalated;

    public ChatResponse(String answer, List<String> sources, boolean escalated) {
        this.answer = answer;
        this.sources = sources;
        this.escalated = escalated;
    }
}
