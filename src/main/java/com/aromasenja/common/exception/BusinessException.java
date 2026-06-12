package com.aromasenja.common.exception;

/**
 * Exception untuk pelanggaran aturan bisnis.
 * Di-map ke HTTP 400 Bad Request oleh GlobalExceptionHandler.
 *
 * Berbeda dengan validasi input (@Valid yang menghasilkan MethodArgumentNotValidException),
 * BusinessException dilempar saat aturan bisnis dilanggar (misal: stok habis,
 * pesanan sudah dibayar, email duplikat, dll).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
