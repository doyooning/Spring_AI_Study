package com.dynii.evaluatesellerbot.service;

import com.dynii.springai.domain.openai.entity.ChatEntity;
import com.dynii.springai.domain.openai.repository.ChatRepository;
import com.dynii.springai.domain.rag.dto.ChatResponse;
import com.dynii.springai.domain.rag.service.ChatSaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final OpenAiChatModel openAiChatModel;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;

    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatRepository chatRepository;
    private final ChatSaveService chatSaveService;

    // Chat 모델
    public ChatResponse generate(String text) {

        ChatClient chatClient = ChatClient.create(openAiChatModel);

        // 메시지
        // SystemMessage = 프롬프팅할 내용
        SystemMessage systemMessage = new SystemMessage("""
                당신은 우리 DESKIT 플랫폼에 회원가입을 신청한 판매자(사업자)들의 사업계획서를 심사하는 AI 심사 봇입니다.
                사업계획서를 분석하고, 제공된 문서를 바탕으로 사업계획서를 심사한 결과를 제시하세요.
                
                결과를 제시할 때, 결과 요약(summary) 필드에는 심사 결과에 대한 이유를 간단히 작성하세요.
                
                

                """);
        // UserMessage = 사용자의 질문
        UserMessage userMessage = new UserMessage(text);
        // AssistantMessage = AI의 답변
        AssistantMessage assistantMessage = new AssistantMessage("");

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);

        // LLM 호출
        ChatResponse response = chatClient.prompt(prompt)
                .tools(new ChatTools())
                .call()
                .entity(ChatResponse.class);

        return response;
    }
}
