package com.dynii.springai.domain.rag.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RagResponse {
    private String answer;
    private List<String> sources;
    private boolean escalated;

    public RagResponse(String answer, List<String> sources, boolean escalated) {
        this.answer = answer;
        this.sources = sources;
        this.escalated = escalated;
    }
}
