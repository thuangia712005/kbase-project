package com.kbase.kbase.service;

import com.kbase.kbase.dto.CreateProjectRequest;
import com.kbase.kbase.dto.InviteMemberRequest;
import com.kbase.kbase.dto.ProjectResponse;
import com.kbase.kbase.entity.Project;
import com.kbase.kbase.entity.ProjectMember;
import com.kbase.kbase.entity.User;
import com.kbase.kbase.repository.ProjectMemberRepository;
import com.kbase.kbase.repository.ProjectRepository;
import com.kbase.kbase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectResponse createProject(String ownerEmail, CreateProjectRequest request) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        Project savedProject = projectRepository.save(project);

        ProjectMember member = ProjectMember.builder()
                .project(savedProject)
                .user(owner)
                .build();
        projectMemberRepository.save(member);

        return toResponse(savedProject);
    }

    public List<ProjectResponse> getMyProjects(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<ProjectMember> memberships = projectMemberRepository.findByUserId(user.getId());

        return memberships.stream()
                .map(m -> toResponse(m.getProject()))
                .toList();
    }

    public ProjectResponse getProjectById(Long projectId, String requesterEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        checkIsMember(project, requesterEmail);

        return toResponse(project);
    }

    public void inviteMember(Long projectId, String requesterEmail, InviteMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // Chỉ Owner mới được mời thành viên
        if (!project.getOwner().getEmail().equals(requesterEmail)) {
            throw new IllegalArgumentException("Only the project owner can invite members");
        }

        User userToInvite = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User with this email does not exist"));

        boolean alreadyMember = projectMemberRepository.findByProjectId(projectId).stream()
                .anyMatch(m -> m.getUser().getId().equals(userToInvite.getId()));

        if (alreadyMember) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(userToInvite)
                .build();
        projectMemberRepository.save(member);
    }

    private void checkIsMember(Project project, String userEmail) {
        boolean isMember = projectMemberRepository.findByProjectId(project.getId()).stream()
                .anyMatch(m -> m.getUser().getEmail().equals(userEmail));

        if (!isMember) {
            throw new IllegalArgumentException("You are not a member of this project");
        }
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerEmail(project.getOwner().getEmail())
                .createdAt(project.getCreatedAt())
                .build();
    }
}