package com.aromasenja.domain.promo;

import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.domain.promo.dto.CreatePromoRequest;
import com.aromasenja.domain.promo.dto.PromoResponse;
import com.aromasenja.domain.promo.entity.TipeDiskon;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromoController.class)
@Import({GlobalExceptionHandler.class, com.aromasenja.config.SecurityConfig.class})
@DisplayName("PromoController MockMvc Tests")
class PromoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PromoService promoService;
    @MockBean private JwtService jwtService;
    @MockBean private com.aromasenja.config.CorsConfig corsConfig;

    private UUID promoId;
    private PromoResponse mockResponse;
    private CreatePromoRequest validRequest;

    @BeforeEach
    void setUp() {
        promoId = UUID.randomUUID();
        mockResponse = new PromoResponse(
                promoId,
                "Diskon Senja",
                TipeDiskon.NOMINAL,
                BigDecimal.valueOf(5000),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                null,
                true,
                null, null, null, null, null
        );

        validRequest = new CreatePromoRequest(
                "Diskon Senja",
                TipeDiskon.NOMINAL,
                BigDecimal.valueOf(5000),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                null, null, null, null, null
        );
    }

    @Test
    @DisplayName("GET /api/promo — 200 OK Public")
    void getActivePromos_200_public() throws Exception {
        when(promoService.getActivePromosForClient()).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/promo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].namaPromo").value("Diskon Senja"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/promo/admin — 200 OK untuk ADMIN")
    void getAllPromosForAdmin_200_admin() throws Exception {
        when(promoService.getAllPromosForAdmin("active")).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/promo/admin")
                .param("status", "active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].namaPromo").value("Diskon Senja"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/promo/admin — 403 Forbidden untuk CLIENT")
    void getAllPromosForAdmin_403_client() throws Exception {
        mockMvc.perform(get("/api/promo/admin")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/promo — 201 Created untuk ADMIN")
    void create_201_admin() throws Exception {
        when(promoService.createPromo(any(CreatePromoRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/promo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.namaPromo").value("Diskon Senja"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/promo/{id} — 200 OK untuk ADMIN")
    void update_200_admin() throws Exception {
        when(promoService.updatePromo(eq(promoId), any(CreatePromoRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/api/promo/{promoId}", promoId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.namaPromo").value("Diskon Senja"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/promo/{id} — 200 OK untuk ADMIN")
    void delete_200_admin() throws Exception {
        doNothing().when(promoService).deletePromo(promoId);

        mockMvc.perform(delete("/api/promo/{promoId}", promoId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/promo/{promoId}/history — 200 OK untuk ADMIN")
    void getPromoHistory_200_admin() throws Exception {
        org.springframework.data.domain.Page<com.aromasenja.domain.promo.dto.PromoHistoryResponse> historyPage =
                new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList());
        when(promoService.getPromoHistory(eq(promoId), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(historyPage);

        mockMvc.perform(get("/api/promo/{promoId}/history", promoId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
