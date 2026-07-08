package com.villo.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // Swagger UI: /swagger-ui/index.html · OpenAPI JSON: /v3/api-docs
    // 인증은 소셜 로그인 후 발급되는 HttpOnly 쿠키(JWT)로 처리 → 같은 브라우저 로그인 상태면 Try it out 동작
    @Bean
    public OpenAPI villoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Villo API")
                        .description("할 일을 퀘스트로 등록하고 마을을 성장시키는 투두 게이미피케이션 앱 — REST API 문서")
                        .version("v1"));
    }
}
