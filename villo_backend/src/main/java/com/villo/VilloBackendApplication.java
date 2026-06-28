package com.villo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // 생성일, 수정일 자동화
@SpringBootApplication
public class VilloBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VilloBackendApplication.class, args);
    }

}
