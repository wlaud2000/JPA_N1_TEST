package com.study.demo.testplayground.domain.post.repository;

import com.study.demo.testplayground.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
