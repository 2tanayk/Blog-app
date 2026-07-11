package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(value = "Post.withUser")
    Page<Post> findByUserId(Long userId, Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long userId);

    @Override
    @EntityGraph(value = "Post.withUser")
    Page<Post> findAll(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id = :id")
    @EntityGraph(value = "Post.withUser")
    Optional<Post> findPostWithUserById(@Param("id") Long id);

    @EntityGraph(value = "Post.withUser")
    Page<Post> findByTags_Name(String tagName, Pageable pageable);
}
