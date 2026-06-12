package com.aromasenja.common.exception;

/**
 * Exception untuk akses yang tidak diizinkan (user ada tapi tidak punya hak akses).
 * Di-map ke HTTP 403 Forbidden oleh GlobalExceptionHandler.
 *
 * Bedakan dengan 401 Unauthorized (token tidak ada/expired) yang dihandle
 * oleh Spring Security secara otomatis.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
