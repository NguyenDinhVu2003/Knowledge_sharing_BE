package com.company.knowledge_sharing_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "document_versions", indexes = {
    @Index(name = "idx_version_document", columnList = "document_id"),
    @Index(name = "idx_version_number", columnList = "version_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Version number is required")
    @Column(nullable = false, name = "version_number")
    private Integer versionNumber;

    @NotBlank(message = "Updated by is required")
    @Column(nullable = false, name = "updated_by", length = 50)
    private String updatedBy;

    @Size(max = 500, message = "Change notes must not exceed 500 characters")
    @Column(name = "change_notes", length = 500)
    private String changeNotes;

    @NotBlank(message = "File path is required")
    @Column(nullable = false, name = "file_path")
    private String filePath;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
}

