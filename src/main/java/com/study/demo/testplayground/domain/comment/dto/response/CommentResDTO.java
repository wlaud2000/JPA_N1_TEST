package com.study.demo.testplayground.domain.comment.dto.response;

import lombok.Builder;

import java.util.List;

public class CommentResDTO {

    @Builder
    public record CommentResponseDTO(
            Long commentId,
            String content,
            String authorNickname,
            PostSummaryDTO post  // 연관된 게시글 정보
    ) {}

    @Builder
    public record PostSummaryDTO(
            Long postId,
            String content,
            String authorNickname
    ) {}

    @Builder
    public record CommentListResponseDTO(
            List<CommentResponseDTO> comments,
            int totalCount,
            String message
    ) {}
}
