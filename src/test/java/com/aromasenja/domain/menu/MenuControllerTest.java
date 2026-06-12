package com.aromasenja.domain.menu;

import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.domain.menu.dto.*;
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
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
@Import({GlobalExceptionHandler.class, com.aromasenja.config.SecurityConfig.class})
@DisplayName("MenuController MockMvc Tests")
class MenuControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private MenuService menuService;
    @MockBean private JwtService jwtService;
    @MockBean private com.aromasenja.config.CorsConfig corsConfig;

    private UUID menuId;
    private MenuDetailResponse mockDetailResponse;
    private MenuResponse mockResponse;

    @BeforeEach
    void setUp() {
        menuId = UUID.randomUUID();
        mockResponse = new MenuResponse(
            menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), "Kopi hitam", "Minuman", true, null, null
        );
        mockDetailResponse = new MenuDetailResponse(
            menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), "Kopi hitam", "Minuman", true, null, null, null, null, null, null, null, null, null, null, null, 4.5
        );
    }

    @Test
    @DisplayName("GET /api/menu — 200 OK Public")
    void getAll_200_public() throws Exception {
        when(menuService.getAllActiveMenus(any(), any(), any())).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/menu")
                .param("category", "Minuman")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].menuName").value("Kopi Tubruk"));
    }

    @Test
    @DisplayName("GET /api/menu/{id} — 200 OK Public")
    void getDetail_200_public() throws Exception {
        when(menuService.getMenuDetail(menuId)).thenReturn(mockDetailResponse);

        mockMvc.perform(get("/api/menu/{id}", menuId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.averageRating").value(4.5));
    }

    @Test
    @DisplayName("GET /api/menu/{id}/pairings — 200 OK Public")
    void getPairings_200_public() throws Exception {
        when(menuService.getPairings(menuId)).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/menu/{id}/pairings", menuId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].menuName").value("Kopi Tubruk"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/menu — 201 Created untuk ADMIN")
    void create_201_admin() throws Exception {
        CreateMenuRequest request = new CreateMenuRequest(
            "Kopi Tubruk", BigDecimal.valueOf(15000), "Kopi hitam", "Minuman", null, null, null, null, null, null, false, Collections.emptyList(), Collections.emptyList()
        );
        when(menuService.createMenu(any(CreateMenuRequest.class), any())).thenReturn(mockDetailResponse);

        mockMvc.perform(post("/api/menu")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.menuName").value("Kopi Tubruk"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/menu — 403 Forbidden untuk CLIENT")
    void create_403_client() throws Exception {
        CreateMenuRequest request = new CreateMenuRequest(
            "Kopi Tubruk", BigDecimal.valueOf(15000), null, "Minuman", null, null, null, null, null, null, false, null, null
        );

        mockMvc.perform(post("/api/menu")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/menu/{id}/availability — 200 OK untuk ADMIN")
    void toggleAvailability_200_admin() throws Exception {
        UpdateMenuAvailabilityRequest request = new UpdateMenuAvailabilityRequest(false);
        mockDetailResponse = new MenuDetailResponse(
            menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), null, "Minuman", false, null, null, null, null, null, null, null, null, null, null, null, 4.5
        );
        when(menuService.toggleAvailability(eq(menuId), any(UpdateMenuAvailabilityRequest.class))).thenReturn(mockDetailResponse);

        mockMvc.perform(patch("/api/menu/{id}/availability", menuId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isAvailable").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/menu/{id} — 200 OK untuk ADMIN")
    void delete_200_admin() throws Exception {
        doNothing().when(menuService).softDeleteMenu(menuId);

        mockMvc.perform(delete("/api/menu/{id}", menuId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
