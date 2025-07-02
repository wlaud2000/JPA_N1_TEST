package com.study.demo.testplayground.domain.post.dto;

public class PostResDTO {

    public record PostDetailResDTO(
            Long id,
            String content
    ) {
    }
}
