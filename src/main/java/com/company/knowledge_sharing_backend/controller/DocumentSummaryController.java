package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.response.SummaryResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.DocumentSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Summary", description = "AI-powered document summary generation (preview)")
public class DocumentSummaryController {

    @Autowired
    private DocumentSummaryService documentSummaryService;

    @Autowired
    private AuthService authService;

    /**
     * Generate AI summary from uploaded file (preview only, does NOT save to database)
     *
     * POST /api/documents/preview-summary
     */
    @Operation(
        summary = "Generate AI summary (preview)",
        description = "Extract text from uploaded file and generate AI summary using GPT. " +
                     "This is a preview endpoint - file is NOT saved to database. " +
                     "Supports: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file type or file too large"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Text extraction or GPT API failed")
    })
    @PostMapping("/preview-summary")
    public ResponseEntity<SummaryResponse> generateSummary(
            @Parameter(description = "File to analyze (PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT)")
            @RequestParam("file") MultipartFile file) {

        // Get current authenticated user
        User currentUser = authService.getCurrentUser();

        // Generate summary (rate limiting applied)
        SummaryResponse response = documentSummaryService.generateSummary(file, currentUser.getId());

        return ResponseEntity.ok(response);
    }
}

