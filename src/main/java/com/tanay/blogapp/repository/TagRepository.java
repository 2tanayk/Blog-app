package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    List<Tag> findByNameIn(Collection<String> names);
}