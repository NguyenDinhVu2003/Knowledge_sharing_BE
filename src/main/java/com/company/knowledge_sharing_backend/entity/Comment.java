package com.company.knowledge_sharing_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "comments",
       indexes = {
           @Index(name = "idx_comment_document", columnList = "document_id"),
           @Index(name = "idx_comment_user", columnList = "user_id"),
           @Index(name = "idx_comment_parent", columnList = "parent_comment_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Comment content is required")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    @Column(nullable = false, length = 2000)
    private String content;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Self-referencing for replies (parent-child relationship)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Comment> replies = new HashSet<>();

    // Likes
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CommentLike> likes = new HashSet<>();

    // Metadata
    @Column(nullable = false)
    @Builder.Default
    private Boolean isEdited = false;

    // Helper methods

    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }

    public int getReplyCount() {
        return replies != null ? replies.size() : 0;
    }

    public void addReply(Comment reply) {
        replies.add(reply);
        reply.setParentComment(this);
    }

    public void removeReply(Comment reply) {
        replies.remove(reply);
        reply.setParentComment(null);
    }
}

