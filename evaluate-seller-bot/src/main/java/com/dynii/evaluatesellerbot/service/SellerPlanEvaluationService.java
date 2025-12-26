package com.dynii.evaluatesellerbot.service;

import com.dynii.evaluatesellerbot.config.RagVectorProperties;
import com.dynii.evaluatesellerbot.dto.EvaluateDTO;
import com.dynii.evaluatesellerbot.entity.AiEvaluation;
import com.dynii.evaluatesellerbot.entity.SellerRegisterEntity;
import com.dynii.evaluatesellerbot.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerPlanEvaluationService {

    // Vector store for policy RAG context lookup.
    private final RedisVectorStore vectorStore;
    // Chat model used to generate the evaluation.
    private final ChatModel chatModel;
    // RAG configuration for retrieval size.
    private final RagVectorProperties ragVectorProperties;
    // Tool collection for structured evaluation output.
    private final ChatTools chatTools;
    // Repository used to persist AI evaluation results.
    private final ChatRepository chatRepository;

    // Evaluate a seller business plan and persist the AI result.
    public AiEvaluation evaluateAndSave(SellerRegisterEntity registerEntity) {
        if (registerEntity == null) {
            throw new IllegalArgumentException("seller register is required");
        }
        if (registerEntity.getPlanFile() == null || registerEntity.getPlanFile().length == 0) {
            throw new IllegalArgumentException("business plan file is required");
        }

        // Extract plan text for evaluation context.
        String planText = extractPlanText(registerEntity.getPlanFile());
        // Retrieve policy context using RAG similarity search.
        String policyContext = buildPolicyContext(planText);

        // System prompt guides AI to use policy context and tool output.
        SystemMessage systemMessage = new SystemMessage("""
                당신은 DESKIT 플랫폼 판매자 회원가입 심사를 담당하는 AI입니다.
                제공된 사업계획서와 정책 문서(Context)를 바탕으로만 평가하세요.
                문서 근거가 부족하면 요약에 그 이유를 명시하고, 추측은 하지 마세요.

                점수는 0~20 범위로 작성하고, total_score는 항목 합계로 작성하세요.
                gradeRecommended는 SellerGrade 열거형 값 중 하나로 지정하세요.

                반드시 getEvaluateResultTool 함수를 호출해서 결과를 반환하세요.
                """);

        // User prompt contains seller data, plan text, and policy context.
        UserMessage userMessage = new UserMessage("""
                [판매자 정보]
                회사명: %s
                설명: %s

                [사업계획서]
                %s

                [정책 문서 발췌]
                %s
                """.formatted(
                nullSafe(registerEntity.getCompanyName()),
                nullSafe(registerEntity.getDescription()),
                planText,
                policyContext
        ));

        // Execute evaluation with tool support for structured output.
        ChatClient chatClient = ChatClient.create(chatModel);
        EvaluateDTO evaluateDTO = chatClient.prompt(new Prompt(List.of(systemMessage, userMessage)))
                .tools(chatTools)
                .call()
                .entity(EvaluateDTO.class);

        // Map DTO to entity for persistence.
        AiEvaluation evaluation = toEntity(evaluateDTO, registerEntity);
        return chatRepository.save(evaluation);
    }

    // Convert evaluation DTO to persisted entity.
    private AiEvaluation toEntity(EvaluateDTO evaluateDTO, SellerRegisterEntity registerEntity) {
        AiEvaluation evaluation = new AiEvaluation();
        evaluation.setBusinessStability(evaluateDTO.businessStability());
        evaluation.setProductCompetency(evaluateDTO.productCompetency());
        evaluation.setLiveSuitability(evaluateDTO.liveSuitability());
        evaluation.setOperationCoop(evaluateDTO.operationCoop());
        evaluation.setGrowthPotential(evaluateDTO.growthPotential());
        evaluation.setTotalScore(evaluateDTO.total_score());
        evaluation.setSellerGrade(evaluateDTO.gradeRecommended());
        evaluation.setSummary(evaluateDTO.summary());
        evaluation.setSellerId(registerEntity.getSellerId());
        evaluation.setRegisterId(registerEntity.getId());
        return evaluation;
    }

    // Extract plain text from the uploaded business plan.
    private String extractPlanText(byte[] planFile) {
        // Convert byte array into a readable resource for Tika.
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(planFile));
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }

    // Build a policy context string using similarity search.
    private String buildPolicyContext(String planText) {
        int topK = ragVectorProperties.getTopK() > 0 ? ragVectorProperties.getTopK() : 4;
        String query = planText.isBlank() ? "판매자 사업계획서 심사 기준" : trimQuery(planText);
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }

    // Trim long text to a reasonable query size for retrieval.
    private String trimQuery(String planText) {
        int limit = 2000;
        return planText.length() > limit ? planText.substring(0, limit) : planText;
    }

    // Normalize null strings for prompt formatting.
    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
