package com.aromasenja.domain.laporan;

import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.domain.laporan.dto.DashboardStatsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LaporanController.class)
@Import({GlobalExceptionHandler.class, com.aromasenja.config.SecurityConfig.class})
@DisplayName("LaporanController MockMvc Tests")
class LaporanControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private LaporanService laporanService;
    @MockBean private JwtService jwtService;
    @MockBean private com.aromasenja.config.CorsConfig corsConfig;

    private DashboardStatsResponse mockDashboardResponse;

    @BeforeEach
    void setUp() {
        mockDashboardResponse = new DashboardStatsResponse(
                BigDecimal.valueOf(250000), 10, 8, 4, 4.5, 3, BigDecimal.valueOf(25000),
                50, BigDecimal.valueOf(5000), Collections.emptyList()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/laporan/dashboard — 200 OK untuk ADMIN")
    void getDashboard_200_admin() throws Exception {
        when(laporanService.getDashboardStats()).thenReturn(mockDashboardResponse);

        mockMvc.perform(get("/api/laporan/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendapatanHariIni").value(250000))
                .andExpect(jsonPath("$.data.totalMejaAktif").value(4));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/laporan/dashboard — 403 Forbidden untuk CLIENT")
    void getDashboard_403_client() throws Exception {
        mockMvc.perform(get("/api/laporan/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/laporan/export — 200 OK untuk ADMIN")
    void exportLaporan_200_admin() throws Exception {
        byte[] mockExcel = new byte[]{1, 2, 3, 4};
        when(laporanService.exportLaporan(any(), any())).thenReturn(mockExcel);

        mockMvc.perform(get("/api/laporan/export")
                .param("period", "bulanan")
                .param("format", "xlsx")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(status().isOk());
    }
}
