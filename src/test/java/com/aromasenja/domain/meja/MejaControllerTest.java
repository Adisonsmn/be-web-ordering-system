package com.aromasenja.domain.meja;

import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.domain.meja.dto.CreateMejaRequest;
import com.aromasenja.domain.meja.dto.MejaResponse;
import com.aromasenja.domain.meja.dto.ScanMejaResponse;
import com.aromasenja.domain.meja.dto.UpdateMejaStatusRequest;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MejaController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("MejaController MockMvc Tests")
class MejaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MejaService mejaService;

    @MockBean
    private JwtService jwtService; // required by JwtAuthFilter auto-configuration

    private UUID mejaId;
    private MejaResponse mockMejaResponse;

    @BeforeEach
    void setUp() {
        mejaId = UUID.randomUUID();
        mockMejaResponse = new MejaResponse(
                mejaId, 5, "INDOOR", true, false, "http://localhost:8080/api/meja/scan/" + mejaId
        );
    }

    @Test
    @DisplayName("GET /api/meja — 200 OK untuk ADMIN")
    @WithMockUser(roles = "ADMIN")
    void GET_meja_200_admin() throws Exception {
        when(mejaService.getAllMeja()).thenReturn(List.of(mockMejaResponse));

        mockMvc.perform(get("/api/meja"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].nomorMeja").value(5))
                .andExpect(jsonPath("$.data[0].zone").value("INDOOR"));

        verify(mejaService).getAllMeja();
    }

    @Test
    @DisplayName("GET /api/meja — 401/403 untuk guest/tanpa auth")
    void GET_meja_403_tanpaAuth() throws Exception {
        mockMvc.perform(get("/api/meja"))
                .andExpect(status().isUnauthorized());

        verify(mejaService, never()).getAllMeja();
    }

    @Test
    @DisplayName("POST /api/meja — 201 Created saat body valid untuk ADMIN")
    @WithMockUser(roles = "ADMIN")
    void POST_meja_201_bodyValid() throws Exception {
        CreateMejaRequest request = new CreateMejaRequest(10, "INDOOR");
        MejaResponse createdMeja = new MejaResponse(
                UUID.randomUUID(), 10, "INDOOR", true, false, "http://localhost:8080/api/meja/scan/some-id"
        );
        when(mejaService.createMeja(any(CreateMejaRequest.class))).thenReturn(createdMeja);

        mockMvc.perform(post("/api/meja")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nomorMeja").value(10))
                .andExpect(jsonPath("$.data.zone").value("INDOOR"));

        verify(mejaService).createMeja(any(CreateMejaRequest.class));
    }

    @Test
    @DisplayName("POST /api/meja — 400 Bad Request saat nomorMeja null")
    @WithMockUser(roles = "ADMIN")
    void POST_meja_400_nomorNull() throws Exception {
        CreateMejaRequest request = new CreateMejaRequest(null, "INDOOR");

        mockMvc.perform(post("/api/meja")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.nomorMeja").exists());

        verify(mejaService, never()).createMeja(any());
    }

    @Test
    @DisplayName("DELETE /api/meja/{mejaId} — 200 OK soft delete untuk ADMIN")
    @WithMockUser(roles = "ADMIN")
    void DELETE_meja_200_sukses() throws Exception {
        doNothing().when(mejaService).softDeleteMeja(mejaId);

        mockMvc.perform(delete("/api/meja/{mejaId}", mejaId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Meja berhasil dihapus"));

        verify(mejaService).softDeleteMeja(mejaId);
    }

    @Test
    @DisplayName("GET /api/meja/{mejaId}/qr — 200 OK dengan content-type image/png")
    @WithMockUser(roles = "ADMIN")
    void GET_qr_200_returnImagePng() throws Exception {
        byte[] dummyQr = new byte[]{1, 2, 3, 4};
        when(mejaService.generateQrCode(mejaId)).thenReturn(dummyQr);

        mockMvc.perform(get("/api/meja/{mejaId}/qr", mejaId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(dummyQr));

        verify(mejaService).generateQrCode(mejaId);
    }

    @Test
    @DisplayName("GET /api/meja/scan/{mejaId} — 200 OK untuk akses publik (tanpa token)")
    @WithMockUser
    void GET_scan_200_publicAccess() throws Exception {
        ScanMejaResponse scanResponse = new ScanMejaResponse(
                mejaId, 5, "INDOOR", true, false, true
        );
        when(mejaService.scanQr(mejaId)).thenReturn(scanResponse);

        mockMvc.perform(get("/api/meja/scan/{mejaId}", mejaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nomorMeja").value(5))
                .andExpect(jsonPath("$.data.isOpen").value(true));

        verify(mejaService).scanQr(mejaId);
    }

    @Test
    @DisplayName("PATCH /api/meja/{mejaId}/status — 200 OK saat update status okupansi")
    @WithMockUser(roles = "ADMIN")
    void PATCH_status_200_updateOccupied() throws Exception {
        UpdateMejaStatusRequest request = new UpdateMejaStatusRequest(true);
        MejaResponse updatedMeja = new MejaResponse(
                mejaId, 5, "INDOOR", true, true, "http://localhost:8080/api/meja/scan/" + mejaId
        );
        when(mejaService.updateStatus(eq(mejaId), any(UpdateMejaStatusRequest.class))).thenReturn(updatedMeja);

        mockMvc.perform(patch("/api/meja/{mejaId}/status", mejaId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isOccupied").value(true));

        verify(mejaService).updateStatus(eq(mejaId), any(UpdateMejaStatusRequest.class));
    }
}
