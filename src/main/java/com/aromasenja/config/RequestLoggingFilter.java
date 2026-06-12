package com.aromasenja.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * HTTP request/response logging filter untuk development.
 *
 * Format log:
 *   ──▶ GET /api/menu?category=Minuman [anon]
 *   ◀── 200 OK (42ms) body={"success":true,...}
 *
 * Fitur:
 * - Log method, URI, query params, user ID (atau "anon")
 * - Log request body (JSON) — skip binary/multipart
 * - Log response status + waktu eksekusi (ms) + body (truncated)
 * - Sensor data sensitif: password, token, secret di-mask
 * - Hanya aktif di profile "dev"
 */
@Slf4j
@Component
@Profile("dev")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_BODY_LOG_LENGTH = 1000;

    /**
     * Pattern untuk mendeteksi field sensitif dalam JSON body.
     * Menangkap: "password":"...", "token":"...", "refreshToken":"...", "secret":"..."
     */
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(\"(?:password|token|refreshToken|accessToken|secret)\"\\s*:\\s*\")([^\"]+)(\")",
            Pattern.CASE_INSENSITIVE
    );

    /** Path yang tidak perlu di-log (actuator, swagger assets, static) */
    private static final String[] SKIP_PATHS = {
            "/actuator", "/swagger-ui", "/api-docs", "/v3/api-docs",
            "/favicon.ico", "/webjars"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip path yang tidak relevan
        String uri = request.getRequestURI();
        for (String skip : SKIP_PATHS) {
            if (uri.startsWith(skip)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Wrap request/response agar body bisa dibaca ulang
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(wrappedRequest);
            logResponse(wrappedResponse, duration);
            // PENTING: copy body kembali ke response asli agar client menerima data
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String user = resolveUser();

        StringBuilder sb = new StringBuilder();
        sb.append("──▶ ").append(method).append(" ").append(uri);
        if (query != null) {
            sb.append("?").append(query);
        }
        sb.append(" [").append(user).append("]");

        // Log request body jika ada (POST/PUT/PATCH)
        byte[] body = request.getContentAsByteArray();
        if (body.length > 0 && isJsonContent(request.getContentType())) {
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            bodyStr = maskSensitiveData(bodyStr);
            bodyStr = truncate(bodyStr);
            sb.append("\n     body=").append(bodyStr);
        }

        log.info("{}", sb);
    }

    private void logResponse(ContentCachingResponseWrapper response, long durationMs) {
        int status = response.getStatus();

        StringBuilder sb = new StringBuilder();
        sb.append("◀── ").append(status).append(" (").append(durationMs).append("ms)");

        // Log response body
        byte[] body = response.getContentAsByteArray();
        if (body.length > 0 && isJsonContent(response.getContentType())) {
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            bodyStr = maskSensitiveData(bodyStr);
            bodyStr = truncate(bodyStr);
            sb.append(" body=").append(bodyStr);
        }

        // Pilih log level berdasarkan status code
        if (status >= 500) {
            log.error("{}", sb);
        } else if (status >= 400) {
            log.warn("{}", sb);
        } else {
            log.info("{}", sb);
        }
    }

    /**
     * Ambil user identifier dari SecurityContext.
     * Return "anon" jika belum terotentikasi.
     */
    private String resolveUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "anon";
        }
        // Ambil nama (biasanya email atau userId)
        return auth.getName();
    }

    /** Mask nilai field sensitif menjadi "***" */
    private String maskSensitiveData(String body) {
        return SENSITIVE_PATTERN.matcher(body).replaceAll("$1***$3");
    }

    /** Potong string jika melebihi batas max */
    private String truncate(String text) {
        if (text.length() <= MAX_BODY_LOG_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_BODY_LOG_LENGTH) + "...(truncated)";
    }

    /** Cek apakah content type adalah JSON */
    private boolean isJsonContent(String contentType) {
        return contentType != null && contentType.toLowerCase().contains("json");
    }
}
