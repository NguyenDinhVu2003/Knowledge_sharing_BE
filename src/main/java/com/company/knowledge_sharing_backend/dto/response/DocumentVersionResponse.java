package com.company.knowledge_sharing_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersionResponse {
    private Long id;
    private Integer versionNumber;
    private String updatedBy;
    private String changeNotes;
    private String filePath;
    private LocalDateTime createdAt;
}

