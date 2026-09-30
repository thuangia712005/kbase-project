package com.kbase.kbase.service;

import com.kbase.kbase.dto.LoginRequest;
import com.kbase.kbase.dto.LoginResponse;
import com.kbase.kbase.dto.SignupRequest;
import com.kbase.kbase.dto.UserResponse;
import com.kbase.kbase.entity.Role;
import com.kbase.kbase.entity.User;
import com.kbase.kbase.repository.UserRepository;
import com.kbase.kbase.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .user(toResponse(user))
                .build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}