package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "File download endpoints (public access)")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Download file
     * GET /api/files/{fileName}
     */
    @Operation(
        summary = "Download file",
        description = "Download document file by filename (public access)"
    )
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File name to download")
            @PathVariable String fileName,
            HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Ignore
        }

        // Fallback to default content type
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}

