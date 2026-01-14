package com.company.knowledge_sharing_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS S3 configuration properties
 * Map from application.properties with prefix 'aws.s3'
 */
@Component
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class S3StorageProperties {

    /**
     * AWS Access Key ID
     * Can be overridden by environment variable: AWS_S3_ACCESS_KEY
     */
    private String accessKey;

    /**
     * AWS Secret Access Key
     * Can be overridden by environment variable: AWS_S3_SECRET_KEY
     */
    private String secretKey;

    /**
     * AWS Region (e.g., ap-southeast-1, us-east-1)
     * Can be overridden by environment variable: AWS_S3_REGION
     */
    private String region;

    /**
     * S3 Bucket name
     * Can be overridden by environment variable: AWS_S3_BUCKET_NAME
     */
    private String bucketName;

    /**
     * Presigned URL expiration time in minutes (default: 60 minutes = 1 hour)
     */
    private int presignedUrlExpirationMinutes = 60;
}

