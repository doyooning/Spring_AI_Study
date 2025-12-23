package com.dynii.springai.domain.openai.service;

import com.dynii.springai.domain.openai.dto.CityResponseDTO;
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
    private final OpenAiImageModel openAiImageModel;
    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;
    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatRepository chatRepository;
    private final ChatSaveService chatSaveService;

    // Chat 모델
    public ChatResponse generate(String text) {

        ChatClient chatClient = ChatClient.create(openAiChatModel);

        // 유저&페이지별 ChatMemory를 관리하기 위한 key (우선은 명시적으로)
        String userId = "dynii1923";

        // 메시지
        // SystemMessage = 프롬프팅할 내용
        SystemMessage systemMessage = new SystemMessage("""
                당신은 고객지원을 대체하는 AI 상담 챗봇입니다.
                
                고객들이 편하게 문의할 수 있도록, "~요"로 끝나도록 대답해주세요.
                
                답변의 길이가 너무 짧아(6글자 이내) 요구사항을 알 수 없을 땐 아래와 같이 대답해주세요.
                - 조금 더 구체적으로 상황을 설명해주시면 정확한 안내를 도와드릴 수 있어요.
                - 질문을 이해하지 못했는데, 다시 한 번 설명해 주실 수 있나요?

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
                .call()
                .entity(ChatResponse.class);

        // 여기서 대화 저장
        chatSaveService.saveChat(userId, text, response.getAnswer());
        chatSaveService.saveChatMemory(userId, text, chatMemoryRepository);

        return response;
    }

    public Flux<String> generateStream(String text) {

        ChatClient chatClient = ChatClient.create(openAiChatModel);

        // 유저&페이지별 ChatMemory를 관리하기 위한 key (우선은 명시적으로)
        String userId = "dynii1923";

        // 전체 대화 저장용
        ChatEntity chatUserEntity = new ChatEntity();
        chatUserEntity.setUserId(userId);
        chatUserEntity.setType(MessageType.USER);
        chatUserEntity.setContent(text);

        // 메시지
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
        chatMemory.add(userId, new UserMessage(text)); // 신규 메시지도 추가

        // 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.7)
                .build();

        // 프롬프트
        Prompt prompt = new Prompt(chatMemory.get(userId), options);

        // 응답 메시지를 저장할 임시 버퍼
        StringBuilder responseBuffer = new StringBuilder();

        // 요청 및 응답
        return chatClient.prompt(prompt)
                .tools(new ChatTools())
                .stream()
                .content()
                .map(token -> {
                    responseBuffer.append(token);
                    return token;
                })
                .doOnComplete(() -> {
                    // chatMemory 저장
                    chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
                    chatMemoryRepository.saveAll(userId, chatMemory.get(userId));

                    // 전체 대화 저장용
                    ChatEntity chatAssistantEntity = new ChatEntity();
                    chatAssistantEntity.setUserId(userId);
                    chatAssistantEntity.setType(MessageType.ASSISTANT);
                    chatAssistantEntity.setContent(responseBuffer.toString());

                    chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
                });
    }

    // Embedding 모델 메서드
    public List<float[]> generateEmbedding(List<String> texts, String model) {

        // 옵션
        EmbeddingOptions embeddingOptions = OpenAiEmbeddingOptions.builder()
                .model(model).build();

        // 프롬프트
        EmbeddingRequest prompt = new EmbeddingRequest(texts, embeddingOptions);

        // 요청 및 응답
        EmbeddingResponse response = openAiEmbeddingModel.call(prompt);
        return response.getResults().stream()
                .map(Embedding::getOutput)
                .toList();
    }

    // 이미지 생성 모델(DALL-E)
    public List<String> generateImages(String text, int count, int height, int width) {

        // 옵션
        OpenAiImageOptions imageOptions = OpenAiImageOptions.builder()
                .quality("hd")
                .N(count)
                .height(height)
                .width(width)
                .build();

        // 프롬프트
        ImagePrompt prompt = new ImagePrompt(text, imageOptions);

        // 요청 및 응답
        ImageResponse response = openAiImageModel.call(prompt);
        return response.getResults().stream()
                .map(image -> image.getOutput().getUrl())
                .toList();
    }

    // TTS : 텍스트를 오디오로
    public byte[] tts(String text) {

        // 옵션
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(1.0f)
                .model(OpenAiAudioApi.TtsModel.TTS_1.value)
                .build();

        // 프롬프트
        SpeechPrompt prompt = new SpeechPrompt(text, speechOptions);

        // 요청 및 응답
        SpeechResponse response = openAiAudioSpeechModel.call(prompt);
        return response.getResult().getOutput();
    }

    // STT : 오디오를 텍스트로
    public String stt(Resource audioFile) {

        // 옵션
        OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.VTT;
        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .language("ko") // 인식할 언어
                .prompt("Ask not this, but ask that") // 음성 인식 전 참고할 텍스트 프롬프트
                .temperature(0f)
                .model(OpenAiAudioApi.TtsModel.TTS_1.value)
                .responseFormat(responseFormat) // 결과 타입 지정 VTT 자막형식
                .build();

        // 프롬프트
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);

        // 요청 및 응답
        AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);
        return response.getResult().getOutput();
    }
}
