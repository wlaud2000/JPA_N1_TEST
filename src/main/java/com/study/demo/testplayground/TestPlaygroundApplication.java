package com.study.demo.testplayground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 스케줄러 활성화
@EnableJpaAuditing //JPA Auditing 기능 활성화
public class TestPlaygroundApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestPlaygroundApplication.class, args);
    }

}
