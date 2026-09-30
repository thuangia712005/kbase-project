package com.kbase.kbase.controller;

import com.kbase.kbase.dto.UserResponse;
import com.kbase.kbase.entity.User;
import com.kbase.kbase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .fullName(u.getFullName())
                        .role(u.getRole())
                        .build())
                .toList();

        return ResponseEntity.ok(users);
    }
}