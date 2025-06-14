package com.study.demo.testplayground.domain.member.dto.response;

import lombok.Builder;

import java.util.List;

public class MemberResDTO {

    @Builder
    public record MemberResponseDTO(
            Long memberId,
            String nickname,
            int postCount,
            int commentCount,
            List<String> postTitles  // 게시글 제목들
    ) {}

    @Builder
    public record MemberListResponseDTO(
            List<MemberResponseDTO> members,
            int totalCount,
            String message
    ) {}
}
