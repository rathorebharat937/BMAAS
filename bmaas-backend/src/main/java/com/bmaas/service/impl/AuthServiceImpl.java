package com.bmaas.service.impl;

import com.bmaas.dto.auth.AuthResponse;
import com.bmaas.dto.auth.LoginRequest;
import com.bmaas.dto.auth.RegisterRequest;
import com.bmaas.entity.User;
import com.bmaas.exception.ResourceAlreadyExistsException;
import com.bmaas.repository.UserRepository;
import com.bmaas.security.JwtTokenProvider;
import com.bmaas.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final com.bmaas.repository.DeveloperRepository developerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository, com.bmaas.repository.DeveloperRepository developerRepository,
                           PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.developerRepository = developerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());

        // Use role from request, default to PROJECT_MANAGER if not provided
        User.UserRole role = User.UserRole.PROJECT_MANAGER;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                role = User.UserRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid role string — keep default
            }
        }
        user.setRole(role);

        User savedUser = userRepository.save(user);

        // If DEVELOPER role, auto-link to matching Developer entity if one exists without a user
        if (role == User.UserRole.DEVELOPER) {
            developerRepository.findByEmailIgnoreCase(savedUser.getEmail()).ifPresent(dev -> {
                if (dev.getUser() == null) {
                    dev.setUser(savedUser);
                    developerRepository.save(dev);
                }
            });
        }

        String token = jwtTokenProvider.generateToken(savedUser.getUsername());

        return new AuthResponse(
            token,
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFullName(),
            savedUser.getRole().name()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return new AuthResponse(
            token,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name()
        );
    }
}