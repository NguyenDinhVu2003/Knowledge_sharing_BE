package com.company.knowledge_sharing_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login credentials")
public class LoginRequest {

    @Schema(description = "Username or email", example = "admin", required = true)
    @NotBlank(message = "Username or email is required")
    private String username;

    @Schema(description = "User password", example = "admin123", required = true)
    @NotBlank(message = "Password is required")
    private String password;
}

