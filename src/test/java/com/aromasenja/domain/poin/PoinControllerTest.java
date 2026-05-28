package com.aromasenja.domain.poin;

import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.domain.poin.dto.PoinBalanceResponse;
import com.aromasenja.domain.poin.dto.PoinKalkulasiRequest;
import com.aromasenja.domain.poin.dto.PoinKalkulasiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PoinController.class)
@Import({GlobalExceptionHandler.class, com.aromasenja.config.SecurityConfig.class})
@DisplayName("PoinController MockMvc Tests")
class PoinControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PoinService poinService;
    @MockBean private JwtService jwtService;
    @MockBean private com.aromasenja.config.CorsConfig corsConfig;

    private PoinBalanceResponse mockBalanceResponse;
    private PoinKalkulasiResponse mockKalkulasiResponse;
    private PoinKalkulasiRequest validRequest;

    @BeforeEach
    void setUp() {
        mockBalanceResponse = new PoinBalanceResponse(120, 100);
        mockKalkulasiResponse = new PoinKalkulasiResponse(BigDecimal.valueOf(1000), BigDecimal.valueOf(9000));
        validRequest = new PoinKalkulasiRequest(BigDecimal.valueOf(10000), 10);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/poin — 200 OK untuk CLIENT")
    void getBalance_200_client() throws Exception {
        when(poinService.getPoinBalance(any())).thenReturn(mockBalanceResponse);

        mockMvc.perform(get("/api/poin")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPoint").value(120))
                .andExpect(jsonPath("$.data.rupiahPerPoin").value(100));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/poin/riwayat — 200 OK untuk CLIENT")
    void getRiwayat_200_client() throws Exception {
        when(poinService.getRiwayatPoin(any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/poin/riwayat")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/poin/kalkulasi — 200 OK untuk CLIENT")
    void kalkulasi_200_client() throws Exception {
        when(poinService.kalkulasiPoin(any(PoinKalkulasiRequest.class), any())).thenReturn(mockKalkulasiResponse);

        mockMvc.perform(post("/api/poin/kalkulasi")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.diskonRupiah").value(1000));
    }
}
