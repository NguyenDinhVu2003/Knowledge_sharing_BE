package com.company.knowledge_sharing_backend.exception;

public class GPTApiException extends RuntimeException {
    public GPTApiException(String message) {
        super(message);
    }

    public GPTApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

