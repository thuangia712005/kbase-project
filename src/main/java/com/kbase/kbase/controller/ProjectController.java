package com.kbase.kbase.controller;

import com.kbase.kbase.dto.*;
import com.kbase.kbase.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            Authentication authentication,
            @Valid @RequestBody CreateProjectRequest request) {

        String email = authentication.getName();
        return ResponseEntity.ok(projectService.createProject(email, request));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(projectService.getMyProjects(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            Authentication authentication,
            @PathVariable Long id) {

        String email = authentication.getName();
        return ResponseEntity.ok(projectService.getProjectById(id, email));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> inviteMember(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody InviteMemberRequest request) {

        String email = authentication.getName();
        projectService.inviteMember(id, email, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse> removeMember(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long userId) {

        String email = authentication.getName();
        projectService.removeMember(id, email, userId);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Member removed successfully")
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody UpdateProjectRequest request) {

        String email = authentication.getName();
        return ResponseEntity.ok(projectService.updateProject(id, email, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProject(
            Authentication authentication,
            @PathVariable Long id) {

        String email = authentication.getName();
        projectService.deleteProject(id, email);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Project deleted successfully")
                        .build()
        );
    }
}