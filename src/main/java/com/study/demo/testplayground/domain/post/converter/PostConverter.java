package com.study.demo.testplayground.domain.post.converter;

import com.study.demo.testplayground.domain.comment.entity.Comment;
import com.study.demo.testplayground.domain.post.dto.response.PostResDTO;
import com.study.demo.testplayground.domain.post.entity.Post;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostConverter {

    // Post 엔티티 -> ResponseDTO
    public static PostResDTO.PostResponseDTO toPostResponseDTO(Post post) {
        return PostResDTO.PostResponseDTO.builder()
                .postId(post.getId())
                .content(post.getContent())
                .authorNickname(post.getMember().getNickname())
                .commentCount(post.getComments().size())
                .comments(post.getComments().stream()
                        .map(PostConverter::toCommentSummaryDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    // Comment -> CommentSummaryDTO
    private static PostResDTO.CommentSummaryDTO toCommentSummaryDTO(Comment comment) {
        return PostResDTO.CommentSummaryDTO.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .authorNickname(comment.getMember().getNickname())
                .build();
    }

    // Post 리스트 -> PostListResponseDTO
    public static PostResDTO.PostListResponseDTO toPostListResponseDTO(List<Post> posts) {
        List<PostResDTO.PostResponseDTO> postList = posts.stream()
                .map(PostConverter::toPostResponseDTO)
                .collect(Collectors.toList());

        return PostResDTO.PostListResponseDTO.builder()
                .posts(postList)
                .totalCount(posts.size())
                .message("🔥 게시글 목록 조회 완료! 콘솔에서 Post-Comment N+1 쿼리를 확인하세요!")
                .build();
    }
}
