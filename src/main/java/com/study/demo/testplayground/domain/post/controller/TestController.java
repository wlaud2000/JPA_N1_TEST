package com.study.demo.testplayground.domain.post.controller;

import com.study.demo.testplayground.domain.comment.dto.response.CommentResDTO;
import com.study.demo.testplayground.domain.comment.service.CommentQueryService;
import com.study.demo.testplayground.domain.post.dto.response.PostResDTO;
import com.study.demo.testplayground.domain.post.service.PostQueryService;
import com.study.demo.testplayground.global.apiPayload.CustomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/n1-test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final PostQueryService postQueryService;
    private final CommentQueryService commentQueryService;

    @GetMapping("/posts")
    public CustomResponse<PostResDTO.PostListResponseDTO> getAllPosts() {
        PostResDTO.PostListResponseDTO result = postQueryService.getAllPosts();
        return CustomResponse.onSuccess(result);
    }

    @GetMapping("/comments")
    public CustomResponse<CommentResDTO.CommentListResponseDTO> getAllComments() {
        CommentResDTO.CommentListResponseDTO result = commentQueryService.getAllComments();
        return CustomResponse.onSuccess(result);
    }
}
