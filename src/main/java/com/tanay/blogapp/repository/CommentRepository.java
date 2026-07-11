package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(value = "Comment.withUser")
    Page<Comment> findByPostId(Long postId, Pageable pageable);

    boolean existsByIdAndUserIdAndPostId(Long id, Long userId, Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.id = :commentId AND c.post.id = :postId")
    void deleteByIdAndPostId(Long commentId, Long postId);
}
