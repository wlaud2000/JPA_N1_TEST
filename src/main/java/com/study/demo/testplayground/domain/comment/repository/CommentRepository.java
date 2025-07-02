package com.study.demo.testplayground.domain.comment.repository;

import com.study.demo.testplayground.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
