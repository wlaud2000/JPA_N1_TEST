package com.study.demo.testplayground.global.data;

import com.study.demo.testplayground.domain.comment.entity.Comment;
import com.study.demo.testplayground.domain.comment.repository.CommentRepository;
import com.study.demo.testplayground.domain.member.entity.Member;
import com.study.demo.testplayground.domain.member.repository.MemberRepository;
import com.study.demo.testplayground.domain.post.entity.Post;
import com.study.demo.testplayground.domain.post.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInit {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @PostConstruct
    @Transactional
    public void initData() {
        log.info("=== 🎬 간단한 N+1 테스트 데이터 생성 시작 ===");

        // 1명의 회원 생성
        Member member = Member.builder()
                .nickname("testuser")
                .password("password123")
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("✅ 회원 생성 완료: {}", savedMember.getNickname());

        // 1개의 게시글 생성
        Post post = Post.builder()
                .member(savedMember)
                .content("N+1 문제 테스트용 게시글입니다. 이 게시글에 댓글 10개가 달려있어요!")
                .build();

        Post savedPost = postRepository.save(post);
        log.info("✅ 게시글 생성 완료: ID {}", savedPost.getId());

        // 10개의 댓글 생성
        for (int i = 1; i <= 10; i++) {
            Comment comment = Comment.builder()
                    .member(savedMember)
                    .post(savedPost)
                    .content(i + "번째 댓글입니다. N+1 문제 테스트용 댓글이에요!")
                    .build();

            commentRepository.save(comment);
        }

        log.info("=== 🎬 간단한 N+1 테스트 데이터 생성 완료 ===");
        log.info("📊 생성된 데이터:");
        log.info("   - 회원: 1명 (testuser)");
        log.info("   - 게시글: 1개");
        log.info("   - 댓글: 10개");
    }
}
