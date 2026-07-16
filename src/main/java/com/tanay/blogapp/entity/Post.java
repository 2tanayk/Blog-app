package com.tanay.blogapp.entity;


import com.tanay.blogapp.entity.type.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
@NamedEntityGraph(
        name = "Post.withUser",
        attributeNodes = @NamedAttributeNode("user")
)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 2048)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // TODO: Handle User Deletion Safeguard
    // Currently, posts require a non-null author (@JoinColumn(nullable = false)).
    // To prevent foreign key constraint violations when a user deletes their profile,
    // choose and implement one of the following strategies later:
    //
    // Option A (Ghost User): Intercept deletion, reassign these posts to a
    // permanent system user (e.g., ID: 1, "Deleted Account"), then delete original user.
    //
    // Option B (Soft Delete): Add a 'deleted' boolean flag to the User entity.
    // Instead of hard-deleting the user, flip the flag to true and mask the UI profile.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(
            mappedBy = "post",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Comment> comments = new ArrayList<>();

    // TODO: might be inefficient, use repository bulk delete or database cascades
    @OneToMany(
            mappedBy = "post",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PostLike> likes = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post post)) return false;
        return id != null && Objects.equals(id, post.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
