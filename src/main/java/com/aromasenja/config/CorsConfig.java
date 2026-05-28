package com.aromasenja.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Konfigurasi CORS.
 * CorsConfigurationSource bean digunakan oleh SecurityConfig agar filter security
 * tidak memblok preflight OPTIONS request dari browser.
 *
 * Dev: izinkan localhost:3000 dan localhost:5173 (Vite dev server).
 * Prod: baca dari env CORS_ALLOWED_ORIGINS (diset di websocket.allowed-origins).
 */
@Configuration
public class CorsConfig {

    @Value("${websocket.allowed-origins:http://localhost:3000}")
    private String allowedOriginsConfig;

    /**
     * Expose CorsConfigurationSource sebagai method (bukan bean langsung)
     * untuk digunakan oleh SecurityConfig. Ini menghindari potential circular bean dependency.
     */
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Gabungkan origins dari config + default dev origins
        List<String> origins = new ArrayList<>();
        Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(origins::add);

        // Selalu tambahkan dev origins (harmless di prod jika tidak diakses dari sana)
        if (!origins.contains("http://localhost:3000"))  origins.add("http://localhost:3000");
        if (!origins.contains("http://localhost:5173"))  origins.add("http://localhost:5173");
        if (!origins.contains("http://localhost:8080"))  origins.add("http://localhost:8080");

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Pre-flight cache 1 jam

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
