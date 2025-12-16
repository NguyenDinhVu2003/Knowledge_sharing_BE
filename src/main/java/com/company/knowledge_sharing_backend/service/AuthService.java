package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.request.LoginRequest;
import com.company.knowledge_sharing_backend.dto.request.RegisterRequest;
import com.company.knowledge_sharing_backend.dto.response.AuthResponse;
import com.company.knowledge_sharing_backend.exception.BadRequestException;
import com.company.knowledge_sharing_backend.entity.Role;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.repository.UserRepository;
import com.company.knowledge_sharing_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Authenticate user and generate JWT token
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = jwtUtil.generateToken(authentication.getName());

        // Get user details
        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsername())
            .orElseThrow(() -> new BadRequestException("User not found"));

        // Return authentication response
        return new AuthResponse(
            jwt,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()
        );
    }

    /**
     * Register new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        // Create new user
        User user = User.builder()
            .username(registerRequest.getUsername())
            .email(registerRequest.getEmail())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .role(Role.EMPLOYEE) // Default role
            .build();

        // Save user
        user = userRepository.save(user);

        // Generate JWT token
        String jwt = jwtUtil.generateToken(user.getUsername());

        // Return authentication response
        return new AuthResponse(
            jwt,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()
        );
    }

    /**
     * Get current authenticated user
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
            .orElseThrow(() -> new BadRequestException("User not found"));
    }
}

