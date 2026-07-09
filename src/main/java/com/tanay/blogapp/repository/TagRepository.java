package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    List<Tag> findByNameIn(Collection<String> names);

    @Modifying(flushAutomatically = true)
    @Query(nativeQuery = true, value = "DELETE FROM post_tag WHERE tag_id = :tagId")
    void deletePostTagAssociations(@Param("tagId") Long tagId);
}