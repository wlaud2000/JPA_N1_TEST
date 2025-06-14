package com.study.demo.testplayground.domain.post.service;

import com.study.demo.testplayground.domain.post.converter.PostConverter;
import com.study.demo.testplayground.domain.post.dto.response.PostResDTO;
import com.study.demo.testplayground.domain.post.entity.Post;
import com.study.demo.testplayground.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public PostResDTO.PostListResponseDTO getAllPosts() {
        long startTime = System.currentTimeMillis();
        log.info("========== 🎬 Post-Comment N+1 테스트 시작 ==========");

        // 1. Post 전체 조회 - EAGER 로딩으로 인해 각 Post마다 Comment들이 자동 조회됨
        List<Post> posts = postRepository.findAll();
        log.info("조회된 게시글 수: {}", posts.size());

        // 2. DTO 변환 과정에서 EAGER 로딩된 comments에 접근
        PostResDTO.PostListResponseDTO result = PostConverter.toPostListResponseDTO(posts);

        long endTime = System.currentTimeMillis();
        log.info("========== 🎬 Post-Comment N+1 테스트 완료 ==========");
        log.info("🔥 실행 시간: {}ms", (endTime - startTime));
        log.info("⚠️ 예상 SQL 쿼리 수:");
        log.info("   - Post 조회: 1개");
        log.info("   - 각 Post의 Comment 조회: {}개 (N+1 문제)", posts.size());
        log.info("   - 총 예상 쿼리 수: {}개", 1 + posts.size());
        log.info("🚨 {} 개의 게시글을 조회하는데 {} 개의 쿼리가 실행됩니다", posts.size(), 1 + posts.size());

        return result;
    }
}
