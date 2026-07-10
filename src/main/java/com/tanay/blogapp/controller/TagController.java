package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.PostSummaryDto;
import com.tanay.blogapp.dto.TagsDto;
import com.tanay.blogapp.service.PostService;
import com.tanay.blogapp.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {
    private final PostService postService;
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<TagsDto> getAllTags(){
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{tagName}/posts")
    public ResponseEntity<Page<PostSummaryDto>> getAllPosts(
            @PathVariable String tagName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getAllPostsByTagName(tagName.trim().toLowerCase(), pageable));
    }


}
