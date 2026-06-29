package com.villo.domain.todo.dto;

import java.util.List;

public record TodoAiRequest(
        String model,
        List<Message> messages
) {
    public record Message(String role, String content) {}

    public static TodoAiRequest of(String model, String todoTitle) {
        String prompt = """
                투두 제목을 분석하여 아래 JSON 형식으로만 응답해줘. 다른 설명은 절대 하지 마.
                
                투두 제목: %s
                
                응답 형식:
                {
                  "category": "카테고리명",
                  "difficulty": "EASY 또는 NORMAL 또는 HARD",
                  "gold": 숫자
                }
                
                규칙:
                - category: 공부, 운동, 생활, 업무, 취미 중 하나
                - difficulty: EASY(단순 반복), NORMAL(약간의 노력), HARD(높은 집중력 필요)
                - gold: EASY=30~60, NORMAL=70~120, HARD=130~200 범위 내 숫자
                """.formatted(todoTitle);

        return new TodoAiRequest(
                model,
                List.of(new Message("user", prompt))
        );
    }
}
