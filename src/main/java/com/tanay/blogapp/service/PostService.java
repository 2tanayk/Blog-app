package com.tanay.blogapp.service;

import com.tanay.blogapp.dto.AddPostDto;
import com.tanay.blogapp.dto.PostDto;
import com.tanay.blogapp.entity.Post;
import com.tanay.blogapp.entity.Tag;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.entity.type.PostStatus;
import com.tanay.blogapp.exception.ResourceNotFoundException;
import com.tanay.blogapp.mapper.PostMapper;
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
    private final PostMapper postMapper;

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
    public Page<PostDto> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);

        return posts.map(postMapper::toDto);
    }

    @Transactional(readOnly = true)
    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));
        return postMapper.toDto(post);
    }

    @PreAuthorize("hasAuthority('POST_EDIT') and (@postSecurity.isOwner(#id, principal) or hasRole('ADMIN'))")
    @Transactional
    public PostDto updatePost(Long id, AddPostDto addPostDto) {
        Post existingPost = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));
        postMapper.updateEntityFromDto(addPostDto, existingPost);

        // existingPost is managed — dirty checking fires UPDATE at commit
        // no save() needed
        existingPost.setTags(resolveTags(addPostDto.tags()));
        return postMapper.toDto(existingPost);
    }

    /**
     * Deletes a post by its ID.
     */

    // TODO: fix this PreAuthorize, workaround for now
    @PreAuthorize("@postSecurity.isOwner(#id, principal) || hasAuthority('POST_DELETE')")
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
    @PreAuthorize("hasAuthority('POST_EDIT') and (@postSecurity.isOwner(#id, principal) or hasRole('ADMIN'))")
    @Transactional
    public PostDto publishPost(Long id) {
        Post existingPost = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));

        existingPost.setStatus(PostStatus.PUBLISHED);

        return postMapper.toDto(existingPost);
    }

    @PreAuthorize("hasAuthority('POST_EDIT') and (@postSecurity.isOwner(#id, principal) or hasRole('ADMIN'))")
    @Transactional
    public PostDto unpublishPost(Long id) {
        Post existingPost = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));

        existingPost.setStatus(PostStatus.DRAFT);

        return postMapper.toDto(existingPost);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getAllPostsByUserId(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserId(userId, pageable);

        return posts.map(postMapper::toDto);
    }

    /**
     * Resolves tag names to Tag entities via batch queries.
     * <p>
     * Normalizes all names, then fires a single SELECT to find existing tags.
     * Missing tags are bulk-saved in one shot — not one-by-one.
     * Total: 2 queries max, regardless of tag count.
     */
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

    public Page<PostDto> getAllPostsByTagName(String tagName, Pageable pageable) {
        Page<Post> posts = postRepository.findByTags_Name(tagName, pageable);

        return posts.map(postMapper::toDto);
    }
}
