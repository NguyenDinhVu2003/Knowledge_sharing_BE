package com.company.knowledge_sharing_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication response with JWT token and user details")
public class AuthResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "john.doe")
    private String username;

    @Schema(description = "User email", example = "john.doe@company.com")
    private String email;

    @Schema(description = "User role", example = "ROLE_EMPLOYEE", allowableValues = {"ROLE_EMPLOYEE", "ROLE_ADMIN"})
    private String role;

    public AuthResponse(String token, Long id, String username, String email, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}

