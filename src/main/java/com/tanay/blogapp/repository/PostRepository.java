package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
