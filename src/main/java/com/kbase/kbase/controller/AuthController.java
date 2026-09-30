package com.kbase.kbase.controller;

import com.kbase.kbase.dto.LoginRequest;
import com.kbase.kbase.dto.LoginResponse;
import com.kbase.kbase.dto.SignupRequest;
import com.kbase.kbase.dto.UserResponse;
import com.kbase.kbase.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(userService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}