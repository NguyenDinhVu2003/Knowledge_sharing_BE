package com.company.knowledge_sharing_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private Long documentId;

    // User info
    private Long userId;
    private String username;

    // Parent comment info (for replies)
    private Long parentCommentId;

    // Metadata
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isLikedByCurrentUser;
    private Boolean isOwnedByCurrentUser;
    private Boolean isEdited;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested replies (optional, only included when requested)
    private List<CommentResponse> replies;
}

