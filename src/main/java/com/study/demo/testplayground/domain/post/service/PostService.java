package com.study.demo.testplayground.domain.post.service;

import com.study.demo.testplayground.domain.post.dto.PostResDTO;
import com.study.demo.testplayground.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

}
