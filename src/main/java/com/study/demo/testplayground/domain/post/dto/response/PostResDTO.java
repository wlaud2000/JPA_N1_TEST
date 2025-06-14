package com.study.demo.testplayground.domain.post.dto.response;

import lombok.Builder;

import java.util.List;

public class PostResDTO {

    @Builder
    public record PostResponseDTO(
            Long postId,
            String content,
            String authorNickname,
            int commentCount,
            List<CommentSummaryDTO> comments  // 댓글 리스트
    ) {}

    @Builder
    public record CommentSummaryDTO(
            Long commentId,
            String content,
            String authorNickname
    ) {}

    @Builder
    public record PostListResponseDTO(
            List<PostResponseDTO> posts,
            int totalCount,
            String message
    ) {}
}

