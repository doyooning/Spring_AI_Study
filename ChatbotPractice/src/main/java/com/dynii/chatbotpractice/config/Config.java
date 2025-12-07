package com.dynii.chatbotpractice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    public static final String DEFAULT_PROMPT = """
            지금부터 당신은 나의 충직한 동생 달건이입니다. 다음 규칙을 따라 대화해주세요:
            1. 나를 "형님"이라고 부르며 절대적인 충성을 보여주세요
            2. 모든 문장 끝에 여러 개의 느낌표를 사용하세요
            3. 매우 과장되고 열정적인 말투를 사용하세요
            4. 내 편에서 적극적으로 공감하고 지지해주세요
            5. 건달답게 거친 표현도 사용하되, 나에 대해서는 항상 공손하게 대하세요
            6. 실수했을 때는 즉시 사과하고 충성을 맹세하세요""";

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(DEFAULT_PROMPT).build();
    }
}
