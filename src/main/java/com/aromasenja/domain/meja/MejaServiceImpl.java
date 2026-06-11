package com.aromasenja.domain.meja;

import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.config_resto.RestoConfigRepository;
import com.aromasenja.domain.config_resto.entity.RestoConfig;
import com.aromasenja.domain.meja.dto.CreateMejaRequest;
import com.aromasenja.domain.meja.dto.MejaResponse;
import com.aromasenja.domain.meja.dto.ScanMejaResponse;
import com.aromasenja.domain.meja.dto.UpdateMejaStatusRequest;
import com.aromasenja.domain.meja.entity.Meja;
import com.aromasenja.domain.meja.entity.MejaSession;
import com.aromasenja.domain.meja.entity.ZoneMeja;
import com.aromasenja.domain.user.AdminRepository;
import com.aromasenja.notification.NotificationService;
import com.aromasenja.notification.payload.MejaStatusWsPayload;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MejaServiceImpl implements MejaService {

    private final MejaRepository mejaRepository;
    private final MejaMapper mejaMapper;
    private final RestoConfigRepository restoConfigRepository;
    private final AdminRepository adminRepository;
    private final NotificationService notificationService;
    private final MejaSessionRepository mejaSessionRepository;

    @Value("${app.qr.base-url}")
    private String qrBaseUrl;

    @Override
    @Transactional(readOnly = true)
    public List<MejaResponse> getAllMeja() {
        List<Meja> mejaList = mejaRepository.findByIsActiveTrue();
        return mejaMapper.toResponseList(mejaList);
    }

    @Override
    @Transactional
    public MejaResponse createMeja(CreateMejaRequest request) {
        // Cek apakah meja dengan nomor tersebut sudah ada
        java.util.Optional<Meja> existingMejaOpt = mejaRepository.findByNomorMeja(request.nomorMeja());
        
        if (existingMejaOpt.isPresent()) {
            Meja existingMeja = existingMejaOpt.get();
            if (existingMeja.isActive()) {
                throw new BusinessException("Nomor meja " + request.nomorMeja() + " sudah digunakan");
            } else {
                // Reactivate meja yang sudah di-soft-delete
                existingMeja.setActive(true);
                existingMeja.setOccupied(false);
                
                ZoneMeja zone = getZone(request.zone());
                existingMeja.setZone(zone);
                
                // Update createdBy
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                    adminRepository.findByUser_Id(principal.getUserId())
                            .ifPresent(admin -> existingMeja.setCreatedBy(admin.getAdminId()));
                }
                
                Meja savedMeja = mejaRepository.save(existingMeja);
                log.info("Meja berhasil diaktifkan kembali: nomor={}, zone={}", savedMeja.getNomorMeja(), savedMeja.getZone());
                return mejaMapper.toResponse(savedMeja);
            }
        }

        ZoneMeja zone = getZone(request.zone());

        Meja meja = new Meja();
        meja.setNomorMeja(request.nomorMeja());
        meja.setZone(zone);
        meja.setActive(true);
        meja.setOccupied(false);

        // Cari admin yang sedang login untuk set created_by
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            adminRepository.findByUser_Id(principal.getUserId())
                    .ifPresent(admin -> meja.setCreatedBy(admin.getAdminId()));
        }

        // Simpan dulu untuk mendapatkan UUID mejaId
        Meja savedMeja = mejaRepository.save(meja);

        // Opsi A: Gunakan mejaId UUID langsung sebagai token/ID di QR URL
        String qrUrl = qrBaseUrl + "?meja=" + savedMeja.getMejaId().toString();
        savedMeja.setQrCodeUrl(qrUrl);

        // Update kembali dengan QR URL
        savedMeja = mejaRepository.save(savedMeja);

        log.info("Meja baru berhasil dibuat: nomor={}, zone={}", savedMeja.getNomorMeja(), savedMeja.getZone());
        return mejaMapper.toResponse(savedMeja);
    }
    
    private ZoneMeja getZone(String zoneStr) {
        try {
            return ZoneMeja.valueOf(zoneStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                return ZoneMeja.fromDbValue(zoneStr);
            } catch (IllegalArgumentException ex) {
                throw new BusinessException("Zona tidak valid: " + zoneStr);
            }
        }
    }

    @Override
    @Transactional
    public void softDeleteMeja(UUID mejaId) {
        if (!mejaRepository.existsById(mejaId)) {
            throw new ResourceNotFoundException("Meja tidak ditemukan");
        }
        mejaRepository.softDelete(mejaId);
        mejaSessionRepository.deactivateSessionByMejaId(mejaId);
        log.info("Meja berhasil di-soft-delete: mejaId={}", mejaId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateQrCode(UUID mejaId) {
        Meja meja = mejaRepository.findById(mejaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meja tidak ditemukan"));

        if (!meja.isActive()) {
            throw new BusinessException("Tidak bisa generate QR Code untuk meja yang tidak aktif");
        }

        String qrUrl = meja.getQrCodeUrl();
        if (qrUrl == null) {
            qrUrl = qrBaseUrl + "?meja=" + mejaId.toString();
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("Gagal generate QR Code untuk mejaId={}", mejaId, e);
            throw new BusinessException("Gagal generate QR Code: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ScanMejaResponse scanQr(UUID mejaId) {
        // Fallback for tests or direct calls where deviceToken is not provided
        return scanQr(mejaId, UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public ScanMejaResponse scanQr(UUID mejaId, String deviceToken) {
        Meja meja = mejaRepository.findByIdWithLock(mejaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meja tidak ditemukan"));

        if (!meja.isActive()) {
            throw new BusinessException("Meja tidak aktif atau sudah dihapus");
        }

        boolean isOpen = restoConfigRepository.findFirstBy()
                .map(RestoConfig::isOpen)
                .orElse(true);

        java.util.Optional<MejaSession> activeSessionOpt = mejaSessionRepository.findByMeja_MejaIdAndIsActiveTrue(mejaId);
        String sessionToken = deviceToken;
        boolean isNewSession = false;

        if (activeSessionOpt.isPresent()) {
            MejaSession activeSession = activeSessionOpt.get();
            if (!activeSession.getDeviceToken().equals(deviceToken)) {
                if (activeSession.getExpiredAt().isBefore(LocalDateTime.now())) {
                    activeSession.setActive(false);
                    mejaSessionRepository.saveAndFlush(activeSession);

                    MejaSession newSession = new MejaSession();
                    newSession.setMeja(meja);
                    newSession.setDeviceToken(deviceToken);
                    newSession.setExpiredAt(LocalDateTime.now().plusHours(4));
                    newSession.setActive(true);
                    mejaSessionRepository.save(newSession);
                    
                    isNewSession = true;
                } else {
                    throw new com.aromasenja.common.exception.ConflictException("Meja sedang digunakan device lain");
                }
            } else {
                activeSession.setExpiredAt(LocalDateTime.now().plusHours(4));
                mejaSessionRepository.save(activeSession);
            }
        } else {
            MejaSession newSession = new MejaSession();
            newSession.setMeja(meja);
            newSession.setDeviceToken(deviceToken);
            newSession.setExpiredAt(LocalDateTime.now().plusHours(4));
            newSession.setActive(true);
            mejaSessionRepository.save(newSession);
            
            isNewSession = true;
        }

        if (isNewSession) {
            meja.setOccupied(true);
            meja = mejaRepository.save(meja);
            
            try {
                notificationService.publishMejaStatus(new MejaStatusWsPayload(
                        meja.getMejaId(),
                        meja.getNomorMeja(),
                        true,
                        "OCCUPIED"
                ));
            } catch (Exception e) {
                log.error("Gagal mengirim notifikasi WebSocket meja occupied saat scan QR: mejaId={}", meja.getMejaId(), e);
            }
        }

        return new ScanMejaResponse(
                meja.getMejaId(),
                meja.getNomorMeja(),
                meja.getZone() != null ? meja.getZone().name() : null,
                meja.isActive(),
                meja.isOccupied(),
                isOpen,
                sessionToken
        );
    }

    @Override
    @Transactional
    public MejaResponse updateStatus(UUID mejaId, UpdateMejaStatusRequest request) {
        Meja meja = mejaRepository.findById(mejaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meja tidak ditemukan"));

        if (!meja.isActive()) {
            throw new BusinessException("Tidak dapat mengubah status meja yang tidak aktif");
        }

        meja.setOccupied(request.isOccupied());

        if (!request.isOccupied()) {
            mejaSessionRepository.deactivateSessionByMejaId(mejaId);
        }
        Meja savedMeja = mejaRepository.save(meja);

        // Tentukan status string untuk WebSocket payload
        String wsStatus = savedMeja.isOccupied() ? "OCCUPIED" : "AVAILABLE";

        // Kirim event WebSocket real-time ke admin dashboard
        notificationService.publishMejaStatus(new MejaStatusWsPayload(
                savedMeja.getMejaId(),
                savedMeja.getNomorMeja(),
                savedMeja.isOccupied(),
                wsStatus
        ));

        log.info("Status meja diperbarui: nomor={}, isOccupied={}, wsStatus={}",
                savedMeja.getNomorMeja(), savedMeja.isOccupied(), wsStatus);
        return mejaMapper.toResponse(savedMeja);
    }
}
