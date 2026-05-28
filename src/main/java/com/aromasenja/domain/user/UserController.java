package com.aromasenja.domain.user;

import com.aromasenja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller untuk manajemen user (admin-facing).
 *
 * Catatan: endpoint profil pengguna aktif (GET/PUT /api/auth/me) sudah dipindah ke AuthController.
 * Controller ini digunakan untuk endpoint user management oleh admin (misal: CRUD member) — dikembangkan
 * bersama domain config_resto sesuai spec endpoint #52–56.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Manajemen pengguna oleh admin — dikembangkan bersama domain config_resto")
public class UserController {

    private final UserService userService;

    // Endpoint admin user management akan ditambahkan bersama domain config_resto
    // Contoh: GET /api/users (list member), PATCH /api/users/{id}, DELETE /api/users/{id}

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("User controller aktif"));
    }
}
