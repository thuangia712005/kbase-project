package com.kbase.kbase.service;

import com.kbase.kbase.dto.DocumentResponse;
import com.kbase.kbase.entity.Document;
import com.kbase.kbase.entity.Project;
import com.kbase.kbase.entity.User;
import com.kbase.kbase.repository.DocumentRepository;
import com.kbase.kbase.repository.ProjectMemberRepository;
import com.kbase.kbase.repository.ProjectRepository;
import com.kbase.kbase.repository.UserRepository;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize MinIO bucket", e);
        }
    }

    public DocumentResponse uploadDocument(Long projectId, String uploaderEmail, MultipartFile file) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isMember = projectMemberRepository.findByProjectId(projectId).stream()
                .anyMatch(m -> m.getUser().getId().equals(uploader.getId()));
        if (!isMember) {
            throw new IllegalArgumentException("You are not a member of this project");
        }

        String storagePath = "project-" + projectId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }

        Document document = Document.builder()
                .project(project)
                .uploadedBy(uploader)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(storagePath)
                .build();

        Document savedDocument = documentRepository.save(document);
        return toResponse(savedDocument);
    }

    public List<DocumentResponse> getDocumentsByProject(Long projectId) {
        return documentRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteDocument(Long documentId, String requesterEmail) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isUploader = document.getUploadedBy().getId().equals(requester.getId());
        boolean isProjectOwner = document.getProject().getOwner().getId().equals(requester.getId());

        if (!isUploader && !isProjectOwner) {
            throw new IllegalArgumentException("You don't have permission to delete this document");
        }

        try {
            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(document.getStoragePath())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }

        documentRepository.delete(document);
    }

    public InputStream downloadDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(document.getStoragePath())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }

    public DocumentResponse getDocumentById(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        return toResponse(document);
    }

    private DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .projectId(document.getProject().getId())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .storagePath(document.getStoragePath())
                .uploadedByEmail(document.getUploadedBy().getEmail())
                .uploadedByName(document.getUploadedBy().getFullName())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}