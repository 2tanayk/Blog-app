package com.tanay.blogapp.service;

import com.tanay.blogapp.dto.AddPostDto;
import com.tanay.blogapp.dto.PostDto;
import com.tanay.blogapp.entity.Post;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.entity.type.PostStatus;
import com.tanay.blogapp.exception.ResourceNotFoundException;
import com.tanay.blogapp.mapper.PostMapper;
import com.tanay.blogapp.repository.PostRepository;
import com.tanay.blogapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final PostMapper postMapper;

    // DOUBT - what is the right approach to create new Post? this or since cascading rules are in
    // place, saving directly through user
    @PreAuthorize("hasAuthority('POST_CREATE')")
    @Transactional
    public PostDto createPost(AddPostDto addPostDto, Long id) {
        //TODO - Create appropriate exception
        //DOUBT - should getReferenceById be used here
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post newPost = postMapper.toEntity(addPostDto);

        newPost.setUser(user);
        // DOUBT - should you do this or not?
        // does this cause perfomance issues?
        //user.getPosts().add(newPost);

        newPost.setStatus(PostStatus.DRAFT);

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
    public List<PostDto> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        return posts.stream().map(postMapper::toDto).toList();
    }

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
        return postMapper.toDto(existingPost);
    }

    // DOUBT - is the below true?
    /**
     * Deletes a post by its ID.
     *
     * @Transactional is used here to bind 'existsById' and 'deleteById' into a single database session.
     * 1. Performance: Reuses Hibernate's session cache, preventing a redundant second SELECT query.
     */

    // TODO - use @Modifying custom query in the repo to make this more efficient
    @PreAuthorize("hasAuthority('POST_DELETE') and (@postSecurity.isOwner(#id, principal) or hasRole('ADMIN'))")
    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post with id " + id + " not found");
        }
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
//        existingPost = postRepository.save(existingPost);
        return postMapper.toDto(existingPost);
    }

    @PreAuthorize("hasAuthority('POST_EDIT') and (@postSecurity.isOwner(#id, principal) or hasRole('ADMIN'))")
    @Transactional
    public PostDto unpublishPost(Long id) {
        Post existingPost = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post with id " + id + " not found"));

        existingPost.setStatus(PostStatus.DRAFT);
//        existingPost = postRepository.save(existingPost);
        return postMapper.toDto(existingPost);
    }

    @Transactional(readOnly = true)
    public List<PostDto>getAllPostsByUserId(Long userId) {
        List<Post> posts = postRepository.findByUserId(userId);

        return posts.stream().map(postMapper::toDto).toList();
    }
}
