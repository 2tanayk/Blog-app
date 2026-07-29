package com.tanay.blogapp.service;

import com.tanay.blogapp.dto.MessageResponseDto;
import com.tanay.blogapp.dto.StatsDto;
import com.tanay.blogapp.entity.Role;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.entity.type.PostStatus;
import com.tanay.blogapp.exception.BadRequestException;
import com.tanay.blogapp.exception.ResourceNotFoundException;
import com.tanay.blogapp.repository.CommentRepository;
import com.tanay.blogapp.repository.PostRepository;
import com.tanay.blogapp.repository.RoleRepository;
import com.tanay.blogapp.repository.TagRepository;
import com.tanay.blogapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;

    @Transactional
    public MessageResponseDto promoteUserToAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found in database"));

        if (user.getRoles().contains(adminRole)) {
            throw new BadRequestException("User is already an admin");
        }

        user.getRoles().add(adminRole);

        return new MessageResponseDto("User promoted to admin");
    }

    @Transactional(readOnly = true)
    public StatsDto getStats() {
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long publishedPosts = postRepository.countByStatus(PostStatus.PUBLISHED);
        long draftPosts = postRepository.countByStatus(PostStatus.DRAFT);
        long totalComments = commentRepository.count();
        long totalTags = tagRepository.count();

        return new StatsDto(totalUsers, totalPosts, publishedPosts, draftPosts, totalComments, totalTags);
    }
}
