package com.study.demo.testplayground.domain.comment.service;

import com.study.demo.testplayground.domain.comment.converter.CommentConverter;
import com.study.demo.testplayground.domain.comment.dto.response.CommentResDTO;
import com.study.demo.testplayground.domain.comment.entity.Comment;
import com.study.demo.testplayground.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentQueryService {

    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public CommentResDTO.CommentListResponseDTO getAllComments() {
        long startTime = System.currentTimeMillis();
        log.info("========== 🎬 Comment-Post N+1 테스트 시작 ==========");

        // 1. Comment 전체 조회 - EAGER 로딩으로 인해 각 Comment마다 Post가 자동 조회됨
        List<Comment> comments = commentRepository.findAll();
        log.info("조회된 댓글 수: {}", comments.size());

        // 2. DTO 변환 과정에서 EAGER 로딩된 post에 접근
        CommentResDTO.CommentListResponseDTO result = CommentConverter.toCommentListResponseDTO(comments);

        long endTime = System.currentTimeMillis();
        log.info("========== 🎬 Comment-Post N+1 테스트 완료 ==========");
        log.info("🔥 실행 시간: {}ms", (endTime - startTime));
        log.info("⚠️ 예상 SQL 쿼리 수:");
        log.info("   - Comment 조회: 1개");
        log.info("   - 각 Comment의 Post 조회: {}개 (N+1 문제)", comments.size());
        log.info("   - 총 예상 쿼리 수: {}개", 1 + comments.size());
        log.info("🚨 {} 개의 댓글을 조회하는데 {} 개의 쿼리가 실행됩니다", comments.size(), 1 + comments.size());

        return result;
    }
}
