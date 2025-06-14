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
        log.info("=== 🎬 N+1 테스트 데이터 생성 시작 ===");

        // 3명의 회원 생성
        Member member1 = Member.builder()
                .nickname("alice")
                .password("password1")
                .build();

        Member member2 = Member.builder()
                .nickname("bob")
                .password("password2")
                .build();

        Member member3 = Member.builder()
                .nickname("charlie")
                .password("password3")
                .build();

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        Member[] members = {member1, member2, member3};

        // 각 회원당 5개의 게시글 생성 (총 15개)
        for (int i = 0; i < 3; i++) {
            Member member = members[i];

            for (int j = 1; j <= 5; j++) {
                Post post = Post.builder()
                        .member(member)
                        .content(member.getNickname() + "의 " + j + "번째 게시글입니다.")
                        .build();

                Post savedPost = postRepository.save(post);

                // 각 게시글당 8개의 댓글 생성
                for (int k = 1; k <= 8; k++) {
                    // 댓글 작성자는 3명 중 순환해서 선택
                    Member commentAuthor = members[k % 3];

                    Comment comment = Comment.builder()
                            .member(commentAuthor)
                            .post(savedPost)
                            .content(commentAuthor.getNickname() + "이 작성한 " + k + "번째 댓글입니다.")
                            .build();

                    commentRepository.save(comment);
                }
            }
        }

        log.info("=== 🎬 N+1 테스트 데이터 생성 완료 ===");
        log.info("📊 생성된 데이터:");
        log.info("   - 회원: 3명 (alice, bob, charlie)");
        log.info("   - 게시글: 15개 (회원당 5개씩)");
        log.info("   - 댓글: 120개 (게시글당 8개씩)");
    }
}
