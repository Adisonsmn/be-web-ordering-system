package com.aromasenja.domain.rating;

import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.domain.rating.dto.CreateRatingRequest;
import com.aromasenja.domain.rating.dto.MenuRatingResponse;
import com.aromasenja.domain.rating.dto.PesananRatingStatusResponse;
import com.aromasenja.domain.rating.dto.RatingResponse;
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

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
@Import({GlobalExceptionHandler.class, com.aromasenja.config.SecurityConfig.class})
@DisplayName("RatingController MockMvc Tests")
class RatingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RatingService ratingService;
    @MockBean private JwtService jwtService;
    @MockBean private com.aromasenja.config.CorsConfig corsConfig;

    private UUID pesananId;
    private UUID menuId;
    private RatingResponse mockResponse;
    private CreateRatingRequest validRequest;

    @BeforeEach
    void setUp() {
        pesananId = UUID.randomUUID();
        menuId = UUID.randomUUID();

        mockResponse = new RatingResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Budi", null, pesananId, (short) 5, "Pelayanan bagus", true, true, null
        );

        validRequest = new CreateRatingRequest(
                pesananId, 5, "Pelayanan bagus", true, Collections.emptyList()
        );
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/rating — 201 Created untuk CLIENT")
    void submitRating_201_client() throws Exception {
        when(ratingService.submitRating(any(CreateRatingRequest.class), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/rating")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ulasan").value("Pelayanan bagus"));
    }

    @Test
    @DisplayName("GET /api/rating/menu/{menuId} — 200 OK Public")
    void getRatingsByMenu_200_public() throws Exception {
        MenuRatingResponse mockMenuResponse = new MenuRatingResponse(4.8, Collections.singletonList(mockResponse));
        when(ratingService.getPublicRatingsByMenu(eq(menuId))).thenReturn(mockMenuResponse);

        mockMvc.perform(get("/api/rating/menu/{menuId}", menuId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.avgRating").value(4.8));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/rating/pesanan/{pesananId} — 200 OK untuk CLIENT")
    void checkRatingStatus_200_client() throws Exception {
        PesananRatingStatusResponse mockStatus = new PesananRatingStatusResponse(true);
        when(ratingService.checkRatingStatus(eq(pesananId), any())).thenReturn(mockStatus);

        mockMvc.perform(get("/api/rating/pesanan/{pesananId}", pesananId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isRated").value(true));
    }
}
