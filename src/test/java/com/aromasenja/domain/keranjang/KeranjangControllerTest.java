package com.aromasenja.domain.keranjang;

import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.domain.keranjang.dto.*;
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

@WebMvcTest(KeranjangController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("KeranjangController MockMvc Tests")
class KeranjangControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private KeranjangService keranjangService;
    @MockBean private JwtService jwtService;

    private UUID cartId;
    private UUID menuId;
    private KeranjangResponse mockResponse;

    @BeforeEach
    void setUp() {
        cartId = UUID.randomUUID();
        menuId = UUID.randomUUID();
        mockResponse = new KeranjangResponse(
            cartId, UUID.randomUUID(), null, List.of(new DetailKeranjangResponse(UUID.randomUUID(), menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), null, 2, "Manis", BigDecimal.valueOf(30000))), BigDecimal.valueOf(30000)
        );
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/keranjang — 200 OK")
    void getCart_200() throws Exception {
        when(keranjangService.getKeranjang(any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/keranjang")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].menuName").value("Kopi Tubruk"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/keranjang/items — 201 Created")
    void addItem_201() throws Exception {
        AddKeranjangItemRequest request = new AddKeranjangItemRequest(menuId, 2, "Manis");
        when(keranjangService.addItem(any(AddKeranjangItemRequest.class), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/keranjang/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("PUT /api/keranjang/items/{id} — 200 OK")
    void updateItem_200() throws Exception {
        UUID detailId = UUID.randomUUID();
        UpdateKeranjangItemRequest request = new UpdateKeranjangItemRequest(4, "Kurang manis");
        mockResponse = new KeranjangResponse(
            cartId, UUID.randomUUID(), null, List.of(new DetailKeranjangResponse(detailId, menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), null, 4, "Kurang manis", BigDecimal.valueOf(60000))), BigDecimal.valueOf(60000)
        );
        when(keranjangService.updateItem(eq(detailId), any(UpdateKeranjangItemRequest.class), any())).thenReturn(mockResponse);

        mockMvc.perform(put("/api/keranjang/items/{detailId}", detailId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].quantity").value(4));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("DELETE /api/keranjang/items/{id} — 200 OK")
    void removeItem_200() throws Exception {
        UUID detailId = UUID.randomUUID();
        mockResponse = new KeranjangResponse(
            cartId, UUID.randomUUID(), null, Collections.emptyList(), BigDecimal.ZERO
        );
        when(keranjangService.removeItem(eq(detailId), any())).thenReturn(mockResponse);

        mockMvc.perform(delete("/api/keranjang/items/{detailId}", detailId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("DELETE /api/keranjang — 200 OK")
    void clearCart_200() throws Exception {
        doNothing().when(keranjangService).clearKeranjang(any());

        mockMvc.perform(delete("/api/keranjang")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
