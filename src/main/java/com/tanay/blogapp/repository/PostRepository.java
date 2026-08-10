package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.Post;
import com.tanay.blogapp.entity.User;
import com.tanay.blogapp.entity.type.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @EntityGraph(value = "Post.withUser")
    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id = :id")
    @EntityGraph(value = "Post.withUser")
    Optional<Post> findPostWithUserById(@Param("id") Long id);

    @EntityGraph(value = "Post.withUser")
    Page<Post> findByTags_Name(String tagName, Pageable pageable);

    @EntityGraph(value = "Post.withUser")
    Page<Post> findByUserIdAndStatus(Long id, PostStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.user = :ghost WHERE p.user.id = :userId")
    void reassignPostsToGhost(@Param("userId") Long userId, @Param("ghost") User ghost);

    long countByStatus(PostStatus status);
}
