package com.tanay.blogapp.repository;

import com.tanay.blogapp.entity.RoleHierarchyMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleHierarchyMappingRepository extends JpaRepository<RoleHierarchyMapping, Long> {
}