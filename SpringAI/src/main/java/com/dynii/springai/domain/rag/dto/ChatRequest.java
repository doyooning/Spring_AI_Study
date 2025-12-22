package com.dynii.springai.domain.rag.dto;

public class ChatRequest {
    private String text;

    public String getQuestion() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
