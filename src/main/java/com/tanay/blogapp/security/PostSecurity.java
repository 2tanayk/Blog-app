package com.tanay.blogapp.security;

import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("postSecurity")
@RequiredArgsConstructor
public class PostSecurity {
    private final PostRepository postRepository;

    public boolean isOwner(Long postId, User user) {
        return postRepository.existsByIdAndUserId(postId, user.getId());
    }
}
