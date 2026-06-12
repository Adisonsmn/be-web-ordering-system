package com.aromasenja.domain.config_resto;

import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.domain.config_resto.dto.MemberResponse;
import com.aromasenja.domain.config_resto.dto.UpdateMemberRequest;
import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@Import({GlobalExceptionHandler.class, com.aromasenja.config.SecurityConfig.class})
@DisplayName("MemberController MockMvc Tests")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private com.aromasenja.config.CorsConfig corsConfig;

    private UUID clientId;
    private MemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        memberResponse = new MemberResponse(
                UUID.randomUUID(),
                clientId,
                "John Doe",
                "john@doe.com",
                "0812",
                100,
                LocalDateTime.now(),
                null,
                true
        );
    }

    @Test
    @DisplayName("GET /api/config/member — 200 OK untuk ADMIN")
    @WithMockUser(roles = "ADMIN")
    void GET_members_200_Admin() throws Exception {
        Page<MemberResponse> page = new PageImpl<>(Collections.singletonList(memberResponse));
        when(memberService.getAllMembers(eq("John"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/config/member?search=John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/config/member/{clientId}/poin-riwayat — 200 OK untuk ADMIN")
    @WithMockUser(roles = "ADMIN")
    void GET_member_poin_riwayat_200_Admin() throws Exception {
        PoinRiwayatResponse riwayat = new PoinRiwayatResponse(
                UUID.randomUUID(), UUID.randomUUID(), "ORD-1", 10, "earn", LocalDateTime.now()
        );
        Page<PoinRiwayatResponse> page = new PageImpl<>(Collections.singletonList(riwayat));
        when(memberService.getMemberPoinRiwayat(eq(clientId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/config/member/" + clientId + "/poin-riwayat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].kodePesanan").value("ORD-1"));
    }

    @Test
    @DisplayName("PATCH /api/config/member/{clientId} — 200 OK untuk ADMIN")
    @WithMockUser(roles = "ADMIN")
    void PATCH_member_200_Admin() throws Exception {
        UpdateMemberRequest request = new UpdateMemberRequest("Jane Doe", "0899", false);
        MemberResponse updatedResponse = new MemberResponse(
                memberResponse.id(), clientId, "Jane Doe", "john@doe.com", "0899", 100, memberResponse.tanggalDaftar(), null, false
        );
        when(memberService.updateMember(eq(clientId), any(UpdateMemberRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/config/member/" + clientId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Jane Doe"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @DisplayName("DELETE /api/config/member/{clientId} — 200 OK untuk ADMIN")
    @WithMockUser(roles = "ADMIN")
    void DELETE_member_200_Admin() throws Exception {
        mockMvc.perform(delete("/api/config/member/" + clientId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/config/member/export — 200 OK untuk ADMIN")
    @WithMockUser(roles = "ADMIN")
    void GET_export_200_Admin() throws Exception {
        byte[] dummyExcel = new byte[]{1, 2, 3};
        when(memberService.exportMembersToExcel()).thenReturn(new ByteArrayInputStream(dummyExcel));

        mockMvc.perform(get("/api/config/member/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"members_export.xlsx\""))
                .andExpect(content().bytes(dummyExcel));
    }
}
