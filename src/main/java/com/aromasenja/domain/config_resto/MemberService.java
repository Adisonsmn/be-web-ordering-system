package com.aromasenja.domain.config_resto;

import com.aromasenja.domain.config_resto.dto.MemberResponse;
import com.aromasenja.domain.config_resto.dto.UpdateMemberRequest;
import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.util.UUID;

public interface MemberService {
    Page<MemberResponse> getAllMembers(String search, Pageable pageable);
    Page<PoinRiwayatResponse> getMemberPoinRiwayat(UUID clientId, Pageable pageable);
    MemberResponse updateMember(UUID clientId, UpdateMemberRequest request);
    void softDeleteMember(UUID clientId);
    ByteArrayInputStream exportMembersToExcel();
}
