package com.aromasenja.common.exception;

/**
 * Exception yang dilempar saat terjadi konflik resource (HTTP 409).
 * Contoh: menghapus menu yang sedang ada di pesanan aktif.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
