package com.dynii.evaluatesellerbot.service;

import com.dynii.evaluatesellerbot.dto.EvaluateDTO;
import com.dynii.evaluatesellerbot.entity.SellerGrade;
import org.springframework.ai.tool.annotation.Tool;

public class ChatTools {
    @Tool(description = "심사 결과 : 사업 안정성, 상품 경쟁력, 라이브커머스 적합도, 운영 협업 가능성, 성장 가능성 및 플랫폼 기여도, " +
            "총점, 배정 그룹, 결과 요약")
    public EvaluateDTO getEvaluateResultTool() {
        return new EvaluateDTO(0, 0,
                0, 0, 0, 0,
                SellerGrade.REJECTED, "text");
    }
}
