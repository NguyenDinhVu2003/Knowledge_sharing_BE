package com.company.knowledge_sharing_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_document_title", columnList = "title"),
    @Index(name = "idx_document_owner", columnList = "owner_id"),
    @Index(name = "idx_document_sharing", columnList = "sharing_level"),
    @Index(name = "idx_document_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "Summary must not exceed 1000 characters")
    @Column(length = 1000)
    private String summary;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @NotBlank(message = "File path is required")
    @Column(nullable = false, name = "file_path")
    private String filePath;

    @NotNull(message = "File type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, name = "file_type")
    private FileType fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @NotNull(message = "Sharing level is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, name = "sharing_level")
    @Builder.Default
    private SharingLevel sharingLevel = SharingLevel.PRIVATE;

    @Column(name = "version_number")
    @Builder.Default
    private Integer versionNumber = 1;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean isArchived = false;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(
        name = "document_tags",
        joinColumns = @JoinColumn(name = "document_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "document_groups",
        joinColumns = @JoinColumn(name = "document_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @Builder.Default
    private Set<Group> groups = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("versionNumber DESC")
    @Builder.Default
    private Set<DocumentVersion> versions = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Rating> ratings = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Favorite> favorites = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Notification> notifications = new HashSet<>();

    // Computed fields

    @Transient
    public Double getAverageRating() {
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToInt(r -> r.getRatingValue())
                .average()
                .orElse(0.0);
    }

    @Transient
    public Integer getRatingCount() {
        return ratings != null ? ratings.size() : 0;
    }

    // Helper methods

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getOwner() {
        return owner;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getDocuments().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getDocuments().remove(this);
    }

    public void addGroup(Group group) {
        groups.add(group);
        group.getDocuments().add(this);
    }

    public void removeGroup(Group group) {
        groups.remove(group);
        group.getDocuments().remove(this);
    }
}
