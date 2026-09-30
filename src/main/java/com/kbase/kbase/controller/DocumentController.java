package com.kbase.kbase.controller;

import com.kbase.kbase.dto.ApiResponse;
import com.kbase.kbase.dto.DocumentResponse;
import com.kbase.kbase.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/project/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file) {

        String email = authentication.getName();
        DocumentResponse response = documentService.uploadDocument(projectId, email, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<DocumentResponse>> listByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(documentService.getDocumentsByProject(projectId));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse> delete(
            Authentication authentication,
            @PathVariable Long documentId) {

        String email = authentication.getName();
        documentService.deleteDocument(documentId, email);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Document deleted successfully")
                        .build()
        );
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long documentId) {
        DocumentResponse document = documentService.getDocumentById(documentId);
        InputStream inputStream = documentService.downloadDocument(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(document.getFileName()).build().toString())
                .body(new InputStreamResource(inputStream));
    }
}