package com.tanay.blogapp.security;

import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("commentSecurity")
@RequiredArgsConstructor
public class CommentSecurity {
    private final CommentRepository commentRepository;

    //TODO: exception throwing might not be appropriate, in case a post doesn't exist
    public boolean isOwner(Long commentId, Long postId, User user) {
        return commentRepository.existsByIdAndUserIdAndPostId(commentId, user.getId(), postId);
    }
}
