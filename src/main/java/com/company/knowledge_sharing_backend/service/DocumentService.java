package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.request.DocumentRequest;
import com.company.knowledge_sharing_backend.dto.request.DocumentSearchRequest;
import com.company.knowledge_sharing_backend.dto.response.DocumentDetailResponse;
import com.company.knowledge_sharing_backend.dto.response.DocumentResponse;
import com.company.knowledge_sharing_backend.dto.response.DocumentVersionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse createDocument(DocumentRequest request, MultipartFile file, Long userId);

    DocumentResponse updateDocument(Long documentId, DocumentRequest request, MultipartFile file, Long userId);

    void archiveDocument(Long documentId, Long userId);

    void deleteDocument(Long documentId, Long userId);

    DocumentDetailResponse getDocumentById(Long documentId, Long userId);

    List<DocumentResponse> getRecentDocuments(int limit, Long userId);

    List<DocumentResponse> getPopularDocuments(int limit, Long userId);

    List<DocumentResponse> getUserDocuments(Long userId, int limit);

    List<DocumentResponse> searchDocuments(DocumentSearchRequest request, Long userId);

    List<DocumentVersionResponse> getDocumentVersions(Long documentId, Long userId);
}

