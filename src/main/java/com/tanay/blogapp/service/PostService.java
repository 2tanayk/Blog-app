package com.tanay.blogapp.service;

import com.tanay.blogapp.dto.*;
import com.tanay.blogapp.entity.*;
import com.tanay.blogapp.entity.type.PostStatus;
import com.tanay.blogapp.exception.ResourceNotFoundException;
import com.tanay.blogapp.mapper.CommentMapper;
import com.tanay.blogapp.mapper.PostMapper;
import com.tanay.blogapp.repository.CommentRepository;
import com.tanay.blogapp.repository.PostRepository;
import com.tanay.blogapp.repository.TagRepository;
import com.tanay.blogapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NOTE:
 * The Hibernate Session is just an in-memory tracker that Spring wraps around your methods via @Transactional.
 * If you keep the box open, Hibernate can optimize your queries, handle lazy loading, and manage states automatically.
 * If you close it too early,
 * you force your app to keep firing redundant database queries.
 */

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;


    // DOUBT - what is the right approach to create new Post? this or since cascading rules are in
    // place, saving directly through user
    @PreAuthorize("hasAuthority('POST_CREATE')")
    @Transactional
    public PostDto createPost(AddPostDto addPostDto, Long id) {
        User user = userRepository.getReferenceById(id);

        Post newPost = postMapper.toEntity(addPostDto);

        newPost.setUser(user);
        // DOUBT - should you do this or not?
        // does this cause performance issues?
        //user.getPosts().add(newPost);

        newPost.setStatus(PostStatus.DRAFT);
        newPost.setTags(resolveTags(addPostDto.tags()));

        Post savedPost = postRepository.save(newPost);
        return postMapper.toDto(savedPost);
    }

    /**
     * Fetches all posts and maps them to DTOs.
     *
     * @Transactional(readOnly = true) is used here for two reasons:
     * 1. Memory Optimization: Disables Hibernate's entity tracking engine, reducing CPU and memory use during stream mapping.
     * 2. Lazy Loading Safety: Keeps the database connection open so MapStruct can safely read any lazy-loaded child fields without crashing.
     */
    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);

        return posts.map(postMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public PostDto getPostById(Long id) {
        Post post = postRepository.findPostWithUserById(id).orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));
        return postMapper.toDto(post);
    }

    @PreAuthorize("hasAuthority('POST_EDIT_ANY') or (hasAuthority('POST_EDIT_OWN') and @postSecurity.isOwner(#id, principal))")
    @Transactional
    public PostDto updatePost(Long id, AddPostDto addPostDto) {
        Post existingPost = postRepository.findPostWithUserById(id).orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));
        postMapper.updateEntityFromDto(addPostDto, existingPost);

        // existingPost is managed — dirty checking fires UPDATE at commit
        // no save() needed
        existingPost.setTags(resolveTags(addPostDto.tags()));
        return postMapper.toDto(existingPost);
    }

    /**
     * Deletes a post by its ID.
     */

    @PreAuthorize("hasAuthority('POST_DELETE_ANY') or (hasAuthority('POST_DELETE_OWN') and @postSecurity.isOwner(#id, principal))")
    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    /**
     * Publishes an existing post.
     *
     * @Transactional is used here to manage the entity inside an active Hibernate session.
     * 1. Auto-Commit: Hibernate automatically detects changes to the status and writes the UPDATE query
     * when the method ends, allowing us to remove the explicit '.save()' call.
     */
    @PreAuthorize("hasAuthority('POST_EDIT_ANY') or (hasAuthority('POST_EDIT_OWN') and @postSecurity.isOwner(#id, principal))")
    @Transactional
    public PostDto publishPost(Long id) {
        Post existingPost = postRepository.findPostWithUserById(id).orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));

        existingPost.setStatus(PostStatus.PUBLISHED);

        return postMapper.toDto(existingPost);
    }

    @PreAuthorize("hasAuthority('POST_EDIT_ANY') or (hasAuthority('POST_EDIT_OWN') and @postSecurity.isOwner(#id, principal))")
    @Transactional
    public PostDto unpublishPost(Long id) {
        Post existingPost = postRepository.findPostWithUserById(id).orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));

        existingPost.setStatus(PostStatus.DRAFT);

        return postMapper.toDto(existingPost);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getAllPostsByUserId(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserId(userId, pageable);

        return posts.map(postMapper::toSummaryDto);
    }

    /**
     * Resolves tag names to Tag entities via batch queries.
     * <p>
     * Normalizes all names, then fires a single SELECT to find existing tags.
     * Missing tags are bulk-saved in one shot — not one-by-one.
     * Total: 2 queries max, regardless of tag count.
     */

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getAllPostsByTagName(String tagName, Pageable pageable) {
        Page<Post> posts = postRepository.findByTags_Name(tagName, pageable);

        return posts.map(postMapper::toSummaryDto);
    }

    private Set<Tag> resolveTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        // Normalize all names upfront
        List<String> normalized = tagNames.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct()
                .toList();

        // One query to find all existing tags
        List<Tag> existingTags = tagRepository.findByNameIn(normalized);
        Set<String> existingNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        // Create entities for names that don't exist yet
        List<Tag> newTags = normalized.stream()
                .filter(name -> !existingNames.contains(name))
                .map(name -> Tag.builder().name(name).build())
                .toList();

        // Bulk save new tags — one INSERT batch
        if (!newTags.isEmpty()) {
            newTags = tagRepository.saveAll(newTags);
        }

        // Combine and return
        Set<Tag> allTags = new HashSet<>(existingTags);
        allTags.addAll(newTags);
        return allTags;
    }

    @PreAuthorize("hasAuthority('COMMENT_CREATE')")
    @Transactional
    public CommentDto addComment(Long postId, AddCommentDto addCommentDto, Long userId) {
        Post existingPost = postRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found"));

        if (existingPost.getStatus() == PostStatus.DRAFT) {
            throw new IllegalArgumentException("Commenting on a draft post is not allowed");
        }

        User author = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        Comment newComment = commentMapper.toEntity(addCommentDto);
        newComment.setUser(author);
        newComment.setPost(existingPost);

        Comment savedComment = commentRepository.save(newComment);

        return commentMapper.toDto(savedComment);
    }

    @Transactional(readOnly = true)
    public Page<CommentDto> getAllCommentsForPost(Long id, Pageable pageable) {
        Page<Comment> posts = commentRepository.findByPostId(id, pageable);

        return posts.map(commentMapper::toDto);
    }

    @PreAuthorize("hasAuthority('COMMENT_DELETE_ANY') or (hasAuthority('COMMENT_DELETE_OWN') and @commentSecurity.isOwner(#commentId, #postId, principal))")
    @Transactional
    public void deleteCommentOnPost(Long postId, Long commentId) {
        commentRepository.deleteByIdAndPostId(commentId, postId);
    }
}
