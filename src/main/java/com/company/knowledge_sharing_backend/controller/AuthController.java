package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.request.LoginRequest;
import com.company.knowledge_sharing_backend.dto.request.RegisterRequest;
import com.company.knowledge_sharing_backend.dto.response.AuthResponse;
import com.company.knowledge_sharing_backend.dto.response.MessageResponse;
import com.company.knowledge_sharing_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Login endpoint
     * POST /api/auth/login
     */
    @Operation(
        summary = "User login",
        description = "Authenticate user and return JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
                content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Register endpoint
     * POST /api/auth/register
     */
    @Operation(
        summary = "User registration",
        description = "Register a new user account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Username or email already exists",
                content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint (client-side token removal)
     * POST /api/auth/logout
     */
    @Operation(
        summary = "User logout",
        description = "Logout user (client should remove token)"
    )
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        // With JWT, logout is typically handled client-side by removing the token
        // This endpoint is here for consistency with Angular frontend
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }

    /**
     * Test authenticated endpoint
     * GET /api/auth/me
     */
    @Operation(
        summary = "Get current user",
        description = "Get authenticated user details",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User details retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}

