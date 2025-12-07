package com.dynii.springai.domain.openai.service;

import com.dynii.springai.domain.openai.dto.UserResponseDTO;
import org.springframework.ai.tool.annotation.Tool;

public class ChatTools {
    @Tool(description = "User personal information : name, age, address, phone, etc")
    public UserResponseDTO getUserInfoTool() {
        return new UserResponseDTO("홍길동", 20L, "서울특별시 강남구 강남대로 1", "010-0000-0000", "00000");
    }
}
