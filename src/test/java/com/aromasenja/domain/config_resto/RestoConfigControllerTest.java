package com.aromasenja.domain.config_resto;

import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.domain.config_resto.dto.RestoConfigResponse;
import com.aromasenja.domain.config_resto.dto.UpdateRestoConfigRequest;
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

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestoConfigController.class)
@Import({GlobalExceptionHandler.class, com.aromasenja.config.SecurityConfig.class})
@DisplayName("RestoConfigController MockMvc Tests")
class RestoConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestoConfigService restoConfigService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private com.aromasenja.config.CorsConfig corsConfig;

    private RestoConfigResponse response;

    @BeforeEach
    void setUp() {
        response = new RestoConfigResponse(
                true,
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                "Aroma Senja",
                "Cita Rasa Nusantara",
                "Alamat",
                "0812",
                "email@resto.com",
                "instagram"
        );
    }

    @Test
    @DisplayName("GET /api/config — 200 OK Public")
    void GET_config_200_Public() throws Exception {
        when(restoConfigService.getConfig()).thenReturn(response);

        mockMvc.perform(get("/api/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.namaRestoran").value("Aroma Senja"));
    }

    @Test
    @DisplayName("PUT /api/config — 200 OK untuk ADMIN")
    @WithMockUser(roles = "ADMIN")
    void PUT_config_200_Admin() throws Exception {
        UpdateRestoConfigRequest request = new UpdateRestoConfigRequest(
                true, LocalTime.of(8, 0), LocalTime.of(22, 0), "Aroma Senja", "New Tagline", "Alamat", "0812", "email@resto.com", "insta"
        );
        when(restoConfigService.updateConfig(any(UpdateRestoConfigRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/config")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/config — 401/403 tanpa ADMIN")
    void PUT_config_403_NoAdmin() throws Exception {
        UpdateRestoConfigRequest request = new UpdateRestoConfigRequest(
                true, LocalTime.of(8, 0), LocalTime.of(22, 0), "Aroma Senja", "New Tagline", "Alamat", "0812", "email@resto.com", "insta"
        );

        mockMvc.perform(put("/api/config")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
