package com.tanay.blogapp.repository;

import com.tanay.blogapp.dto.PostLikeCountDto;
import com.tanay.blogapp.entity.PostLike;
import com.tanay.blogapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long id);

    @Query("SELECT new com.tanay.blogapp.dto.PostLikeCountDto(pl.post.id, COUNT(pl)) " +
            "FROM PostLike pl WHERE pl.post.id IN :postIds GROUP BY pl.post.id")
    List<PostLikeCountDto> countLikesByPostIds(@Param("postIds") List<Long> postIds);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PostLike pl SET pl.user = :ghost WHERE pl.user.id = :userId")
    void reassignLikesToGhost(@Param("userId") Long userId, @Param("ghost") User ghost);
}