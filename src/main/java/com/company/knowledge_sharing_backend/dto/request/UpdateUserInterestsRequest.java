package com.company.knowledge_sharing_backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request to update user interests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInterestsRequest {

    @NotEmpty(message = "At least one tag ID is required")
    private List<Long> tagIds;
}

