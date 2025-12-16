package com.company.knowledge_sharing_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Summary must not exceed 1000 characters")
    private String summary;

    private String content; // Optional: for text-based documents

    private List<String> tags; // Tag names

    @NotBlank(message = "Sharing level is required")
    private String sharingLevel; // "PRIVATE", "GROUP", "PUBLIC"

    private List<Long> groupIds; // Required if sharingLevel is GROUP

    private String changeNotes; // For versioning on update
}

