package com.tanay.blogapp.controller;

import com.tanay.blogapp.dto.PostSummaryDto;
import com.tanay.blogapp.dto.TagsDto;
import com.tanay.blogapp.service.PostService;
import com.tanay.blogapp.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Tag(name = "Tags", description = "Endpoints for browsing tags and filtering posts by tag")
public class TagController {
    private final PostService postService;
    private final TagService tagService;

    @Operation(summary = "List all tags", description = "Returns the names of all available tags")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of tags")
    })
    @GetMapping
    public ResponseEntity<TagsDto> getAllTags() {
        log.info("GET /tags - listing all tags");
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @Operation(summary = "List posts by tag", description = "Paginated list of posts tagged with the given tag name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated post listing")
    })
    @GetMapping("/{tagName}/posts")
    public ResponseEntity<Page<PostSummaryDto>> getAllPosts(
            @PathVariable String tagName,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /tags/{}/posts - listing posts by tag, page={}", tagName, pageable.getPageNumber());
        return ResponseEntity.ok(postService.getAllPostsByTagName(tagName.trim().toLowerCase(), pageable));
    }
}