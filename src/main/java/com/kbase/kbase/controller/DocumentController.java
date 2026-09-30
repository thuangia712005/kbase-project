package com.kbase.kbase.controller;

import com.kbase.kbase.entity.Document;
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
    public ResponseEntity<Document> upload(
            Authentication authentication,
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file) {

        String email = authentication.getName();
        Document document = documentService.uploadDocument(projectId, email, file);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Document>> listByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(documentService.getDocumentsByProject(projectId));
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long documentId) {
        Document document = documentService.getDocumentById(documentId);
        InputStream inputStream = documentService.downloadDocument(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(document.getFileName()).build().toString())
                .body(new InputStreamResource(inputStream));
    }
}
