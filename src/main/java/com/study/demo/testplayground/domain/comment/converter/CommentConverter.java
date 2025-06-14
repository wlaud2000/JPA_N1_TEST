package com.study.demo.testplayground.domain.comment.converter;

import com.study.demo.testplayground.domain.comment.dto.response.CommentResDTO;
import com.study.demo.testplayground.domain.comment.entity.Comment;
import com.study.demo.testplayground.domain.post.entity.Post;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentConverter {

    // Comment 엔티티 -> ResponseDTO
    public static CommentResDTO.CommentResponseDTO toCommentResponseDTO(Comment comment) {
        return CommentResDTO.CommentResponseDTO.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .authorNickname(comment.getMember().getNickname())
                .post(toPostSummaryDTO(comment.getPost()))
                .build();
    }

    // Post -> PostSummaryDTO
    private static CommentResDTO.PostSummaryDTO toPostSummaryDTO(Post post) {
        return CommentResDTO.PostSummaryDTO.builder()
                .postId(post.getId())
                .content(post.getContent())
                .authorNickname(post.getMember().getNickname())
                .build();
    }

    // Comment 리스트 -> CommentListResponseDTO
    public static CommentResDTO.CommentListResponseDTO toCommentListResponseDTO(List<Comment> comments) {
        List<CommentResDTO.CommentResponseDTO> commentList = comments.stream()
                .map(CommentConverter::toCommentResponseDTO)
                .collect(Collectors.toList());

        return CommentResDTO.CommentListResponseDTO.builder()
                .comments(commentList)
                .totalCount(comments.size())
                .message("🔥 댓글 목록 조회 완료! 콘솔에서 Comment-Post N+1 쿼리를 확인하세요!")
                .build();
    }
}
