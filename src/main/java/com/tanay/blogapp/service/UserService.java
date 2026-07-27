package com.tanay.blogapp.service;

import com.tanay.blogapp.dto.PostSummaryDto;
import com.tanay.blogapp.dto.UpdateUserPasswordDto;
import com.tanay.blogapp.dto.UpdateUserProfileDto;
import com.tanay.blogapp.dto.UpdateUserPasswordDto;
import com.tanay.blogapp.dto.UserProfileDto;
import com.tanay.blogapp.dto.MessageResponseDto;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.exception.BadRequestException;
import com.tanay.blogapp.exception.ResourceNotFoundException;
import com.tanay.blogapp.mapper.UserMapper;
import com.tanay.blogapp.repository.CommentRepository;
import com.tanay.blogapp.repository.PostLikeRepository;
import com.tanay.blogapp.repository.PostRepository;
import com.tanay.blogapp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String GHOST_EMAIL = "deleted@blog.com";

    public UserProfileDto getUserProfileDetails(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        return userMapper.toDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long id, UpdateUserProfileDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.bio() != null) {
            user.setBio(request.bio());
        }

        return userMapper.toDto(user);
    }

    @Transactional
    public MessageResponseDto updatePassword(Long id, UpdateUserPasswordDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        if (user.getPassword() == null) {
            throw new BadRequestException("OAuth accounts don't have passwords");
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        return new MessageResponseDto("Password updated successfully");
    }

    @Transactional
    public MessageResponseDto deleteUser(Long userId) {
        User ghost = userRepository.findByEmail(GHOST_EMAIL)
                .orElseThrow(() -> new IllegalStateException("Ghost user not found in database"));

        postRepository.reassignPostsToGhost(userId, ghost);
        commentRepository.reassignCommentsToGhost(userId, ghost);
        postLikeRepository.reassignLikesToGhost(userId, ghost);

        userRepository.deleteById(userId);

        return new MessageResponseDto("Account deleted successfully");
    }
}
