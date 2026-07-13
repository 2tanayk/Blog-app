package com.tanay.blogapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role_hierarchy")
public class RoleHierarchyMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "higher_role", nullable = false)
    private String higherRole;

    @Column(name = "lower_role", nullable = false)
    private String lowerRole;
}
