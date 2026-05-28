package com.aromasenja.domain.config_resto;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.domain.config_resto.dto.MemberResponse;
import com.aromasenja.domain.config_resto.dto.UpdateMemberRequest;
import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "Manajemen Member & Konfigurasi", description = "Endpoints untuk mengelola member terdaftar, riwayat poin member, dan ekspor excel member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil semua member terdaftar (Admin Only)", description = "Mendukung pencarian nama/email dan paginasi.")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> getAllMembers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MemberResponse> response = memberService.getAllMembers(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Daftar member berhasil diambil", response));
    }

    @GetMapping("/member/{clientId}/poin-riwayat")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil riwayat transaksi poin member (Admin Only)", description = "Histori earn & redeem poin dari member tertentu.")
    public ResponseEntity<ApiResponse<Page<PoinRiwayatResponse>>> getMemberPoinRiwayat(
            @PathVariable UUID clientId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PoinRiwayatResponse> response = memberService.getMemberPoinRiwayat(clientId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Riwayat poin member berhasil diambil", response));
    }

    @PatchMapping("/member/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Edit data member (Admin Only)", description = "Memperbarui nama, telepon, dan status aktif member.")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @PathVariable UUID clientId,
            @Valid @RequestBody UpdateMemberRequest request) {
        MemberResponse response = memberService.updateMember(clientId, request);
        return ResponseEntity.ok(ApiResponse.success("Data member berhasil diperbarui", response));
    }

    @DeleteMapping("/member/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Hapus member / Soft Delete (Admin Only)", description = "Menonaktifkan member dengan mengubah is_active = false.")
    public ResponseEntity<ApiResponse<Void>> softDeleteMember(
            @PathVariable UUID clientId) {
        memberService.softDeleteMember(clientId);
        return ResponseEntity.ok(ApiResponse.success("Member berhasil dinonaktifkan (soft delete)"));
    }

    @GetMapping("/member/export")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Export data member ke Excel (Admin Only)", description = "Mendownload format xlsx data seluruh member terdaftar.")
    public ResponseEntity<Resource> exportMembers() {
        ByteArrayInputStream in = memberService.exportMembersToExcel();
        try {
            byte[] bytes = in.readAllBytes();
            ByteArrayResource resource = new ByteArrayResource(bytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"members_export.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(bytes.length)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Gagal export data member", e);
        }
    }
}
