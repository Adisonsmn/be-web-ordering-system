package com.aromasenja.domain.meja;

import com.aromasenja.domain.meja.dto.CreateMejaRequest;
import com.aromasenja.domain.meja.dto.MejaResponse;
import com.aromasenja.domain.meja.dto.ScanMejaResponse;
import com.aromasenja.domain.meja.dto.UpdateMejaStatusRequest;

import java.util.List;
import java.util.UUID;

public interface MejaService {
    List<MejaResponse> getAllMeja();
    MejaResponse createMeja(CreateMejaRequest request);
    void softDeleteMeja(UUID mejaId);
    byte[] generateQrCode(UUID mejaId);
    ScanMejaResponse scanQr(UUID mejaId);
    ScanMejaResponse scanQr(UUID mejaId, String deviceToken);
    MejaResponse updateStatus(UUID mejaId, UpdateMejaStatusRequest request);
}
