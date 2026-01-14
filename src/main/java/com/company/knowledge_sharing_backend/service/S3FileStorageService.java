package com.company.knowledge_sharing_backend.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.company.knowledge_sharing_backend.config.S3StorageProperties;
import com.company.knowledge_sharing_backend.entity.FileType;
import com.company.knowledge_sharing_backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.*;

/**
 * S3 File Storage Service
 * Handles file upload, download, and deletion from AWS S3
 */
@Service
@Slf4j
public class S3FileStorageService {

    @Autowired
    private AmazonS3 amazonS3Client;

    @Autowired
    private S3StorageProperties s3Properties;

    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "jpg", "jpeg", "png"
    );

    // Max file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Upload file to S3 and return S3 key
     */
    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        // Generate unique S3 key
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String s3Key = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Prepare metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // Upload to S3
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    s3Properties.getBucketName(),
                    s3Key,
                    file.getInputStream(),
                    metadata
            );

            // Note: Bucket has ACLs disabled - using presigned URLs for access
            // No need to set public ACL

            amazonS3Client.putObject(putObjectRequest);

            log.info("File uploaded successfully to S3: {}", s3Key);
            return s3Key;

        } catch (AmazonServiceException e) {
            log.error("AWS S3 error while uploading file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to S3: " + e.getErrorMessage(), e);
        } catch (IOException e) {
            log.error("IO error while uploading file: {}", e.getMessage());
            throw new RuntimeException("Failed to read file for upload: " + e.getMessage(), e);
        }
    }

    /**
     * Generate presigned URL for file download
     * URL is valid for configured duration (default: 1 hour)
     */
    public String generatePresignedUrl(String s3Key) {
        try {
            Date expiration = new Date();
            long expirationTimeMillis = expiration.getTime();
            expirationTimeMillis += Duration.ofMinutes(s3Properties.getPresignedUrlExpirationMinutes()).toMillis();
            expiration.setTime(expirationTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                    s3Properties.getBucketName(),
                    s3Key
            )
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);

            URL url = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
            return url.toString();

        } catch (AmazonServiceException e) {
            log.error("AWS S3 error while generating presigned URL: {}", e.getMessage());
            throw new RuntimeException("Failed to generate presigned URL: " + e.getErrorMessage(), e);
        }
    }

    /**
     * Get file as InputStream (for direct streaming)
     */
    public InputStream getFileAsInputStream(String s3Key) {
        try {
            S3Object s3Object = amazonS3Client.getObject(s3Properties.getBucketName(), s3Key);
            return s3Object.getObjectContent();

        } catch (AmazonServiceException e) {
            if (e.getStatusCode() == 404) {
                throw new RuntimeException("File not found in S3: " + s3Key);
            }
            log.error("AWS S3 error while retrieving file: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve file from S3: " + e.getErrorMessage(), e);
        }
    }

    /**
     * Get object metadata
     */
    public ObjectMetadata getFileMetadata(String s3Key) {
        try {
            return amazonS3Client.getObjectMetadata(s3Properties.getBucketName(), s3Key);
        } catch (AmazonServiceException e) {
            if (e.getStatusCode() == 404) {
                throw new RuntimeException("File not found in S3: " + s3Key);
            }
            log.error("AWS S3 error while retrieving metadata: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve file metadata: " + e.getErrorMessage(), e);
        }
    }

    /**
     * Delete file from S3
     */
    public void deleteFile(String s3Key) {
        try {
            amazonS3Client.deleteObject(s3Properties.getBucketName(), s3Key);
            log.info("File deleted successfully from S3: {}", s3Key);

        } catch (AmazonServiceException e) {
            log.error("AWS S3 error while deleting file: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file from S3: " + e.getErrorMessage(), e);
        }
    }

    /**
     * Check if file exists in S3
     */
    public boolean fileExists(String s3Key) {
        try {
            return amazonS3Client.doesObjectExist(s3Properties.getBucketName(), s3Key);
        } catch (AmazonServiceException e) {
            log.error("AWS S3 error while checking file existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get public URL for file (if bucket is public)
     */
    public String getPublicUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Properties.getBucketName(),
                s3Properties.getRegion(),
                s3Key);
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 10MB");
        }

        // Check file extension
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);

        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new BadRequestException("File type not allowed. Allowed types: PDF, DOC, DOCX, JPG, JPEG, PNG");
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    /**
     * Determine FileType enum from extension
     */
    public FileType determineFileType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();

        switch (extension) {
            case "pdf":
                return FileType.PDF;
            case "doc":
            case "docx":
                return FileType.DOC;
            case "jpg":
            case "jpeg":
            case "png":
                return FileType.IMAGE;
            default:
                throw new BadRequestException("Unsupported file type: " + extension);
        }
    }

    /**
     * List all files in bucket (for admin/debugging purposes)
     */
    public List<String> listAllFiles() {
        List<String> keys = new ArrayList<>();
        try {
            ObjectListing objectListing = amazonS3Client.listObjects(s3Properties.getBucketName());
            for (S3ObjectSummary summary : objectListing.getObjectSummaries()) {
                keys.add(summary.getKey());
            }
            return keys;
        } catch (AmazonServiceException e) {
            log.error("AWS S3 error while listing files: {}", e.getMessage());
            throw new RuntimeException("Failed to list files from S3: " + e.getErrorMessage(), e);
        }
    }
}

