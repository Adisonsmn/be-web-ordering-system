package com.aromasenja.domain.config_resto;

import com.aromasenja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Endpoint untuk memastikan service backend berjalan dengan normal")
public class HealthController {

    @GetMapping
    @Operation(summary = "Cek status aplikasi", description = "Diakses publik tanpa token untuk memonitor apakah aplikasi aktif")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> statusInfo = Map.of(
                "status", "UP",
                "message", "Aroma Senja API is running smoothly!",
                "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(
                ApiResponse.success("Service is healthy", statusInfo)
        );
    }
}
