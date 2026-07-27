package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.Comment;
import com.tanay.blogapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(value = "Comment.withUser")
    Page<Comment> findByPostId(Long postId, Pageable pageable);

    boolean existsByIdAndUserIdAndPostId(Long id, Long userId, Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.id = :commentId AND c.post.id = :postId")
    void deleteByIdAndPostId(Long commentId, Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c SET c.user = :ghost WHERE c.user.id = :userId")
    void reassignCommentsToGhost(@Param("userId") Long userId, @Param("ghost") User ghost);
}
