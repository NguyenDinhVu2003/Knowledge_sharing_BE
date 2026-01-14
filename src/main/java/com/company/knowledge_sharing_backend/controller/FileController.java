package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.service.S3FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "File access endpoints (public access)")
public class FileController {

    @Autowired
    private S3FileStorageService s3FileStorageService;

    /**
     * Get presigned URL for file download/view
     * GET /api/files/{s3Key}
     */
    @Operation(
        summary = "Get file URL",
        description = "Get presigned URL to access file from S3 (valid for 1 hour)"
    )
    @GetMapping("/{s3Key:.+}")
    public ResponseEntity<Map<String, String>> getFileUrl(
            @Parameter(description = "S3 key of the file")
            @PathVariable String s3Key) {

        // Generate presigned URL (valid for 1 hour)
        String presignedUrl = s3FileStorageService.generatePresignedUrl(s3Key);

        Map<String, String> response = new HashMap<>();
        response.put("url", presignedUrl);
        response.put("expiresIn", "3600"); // seconds (1 hour)
        response.put("s3Key", s3Key);

        return ResponseEntity.ok(response);
    }

    /**
     * Check if file exists
     * HEAD /api/files/{s3Key}
     */
    @Operation(
        summary = "Check file existence",
        description = "Check if file exists in S3"
    )
    @RequestMapping(value = "/{s3Key:.+}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkFileExists(
            @Parameter(description = "S3 key of the file")
            @PathVariable String s3Key) {

        boolean exists = s3FileStorageService.fileExists(s3Key);

        if (exists) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

