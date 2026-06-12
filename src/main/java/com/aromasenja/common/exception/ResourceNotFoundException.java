package com.aromasenja.common.exception;

/**
 * Exception untuk resource yang tidak ditemukan di database.
 * Di-map ke HTTP 404 Not Found oleh GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
