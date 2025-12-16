package com.company.knowledge_sharing_backend.repository;

import com.company.knowledge_sharing_backend.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    /**
     * Find all versions of a document (ordered by version number descending)
     */
    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(Long documentId);

    /**
     * Find latest version of a document
     */
    @Query("SELECT v FROM DocumentVersion v WHERE v.document.id = :documentId ORDER BY v.versionNumber DESC LIMIT 1")
    DocumentVersion findLatestVersionByDocumentId(@Param("documentId") Long documentId);

    /**
     * Find specific version of a document
     */
    DocumentVersion findByDocumentIdAndVersionNumber(Long documentId, Integer versionNumber);

    /**
     * Count versions of a document
     */
    Long countByDocumentId(Long documentId);

    /**
     * Delete all versions of a document
     */
    void deleteByDocumentId(Long documentId);
}

