package com.company.knowledge_sharing_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String content;
}

