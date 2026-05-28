package com.aromasenja.domain.config_resto;

import com.aromasenja.domain.config_resto.dto.RestoConfigResponse;
import com.aromasenja.domain.config_resto.dto.UpdateRestoConfigRequest;
import com.aromasenja.domain.config_resto.entity.RestoConfig;
import com.aromasenja.notification.NotificationService;
import com.aromasenja.notification.payload.RestoStatusWsPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class RestoConfigServiceImpl implements RestoConfigService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestoConfigServiceImpl.class);

    private final RestoConfigRepository restoConfigRepository;
    private final ConfigMapper configMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RestoConfigResponse getConfig() {
        RestoConfig config = restoConfigRepository.findFirstBy()
                .orElseGet(this::createDefaultConfig);
        return configMapper.toResponse(config);
    }

    @Override
    @Transactional
    public RestoConfigResponse updateConfig(UpdateRestoConfigRequest request) {
        RestoConfig config = restoConfigRepository.findFirstBy()
                .orElseGet(this::createDefaultConfig);

        boolean oldIsOpen = config.isOpen();
        boolean newIsOpen = request.isOpen();

        config.setOpen(newIsOpen);
        config.setOpenTime(request.openTime());
        config.setCloseTime(request.closeTime());
        config.setNamaRestoran(request.nama());
        config.setTagline(request.tagline());
        config.setAlamat(request.alamat());
        config.setTelepon(request.telepon());
        config.setEmail(request.email());
        config.setInstagram(request.instagram());
        config.setUpdatedAt(LocalDateTime.now());

        RestoConfig savedConfig = restoConfigRepository.save(config);

        // Jika status is_open berubah, broadcast via WebSocket
        if (oldIsOpen != newIsOpen) {
            String message = newIsOpen ? "Restoran telah buka!" : "Restoran telah tutup!";
            RestoStatusWsPayload payload = new RestoStatusWsPayload(newIsOpen, message, LocalDateTime.now());
            notificationService.publishRestoStatus(payload);
            log.info("Restaurant status changed. Broadcast sent. isOpen={}", newIsOpen);
        }

        return configMapper.toResponse(savedConfig);
    }

    private RestoConfig createDefaultConfig() {
        RestoConfig config = new RestoConfig();
        config.setOpen(true);
        config.setOpenTime(LocalTime.of(8, 0));
        config.setCloseTime(LocalTime.of(22, 0));
        config.setNamaRestoran("Aroma Senja");
        config.setTagline("Cita Rasa Nusantara");
        config.setUpdatedAt(LocalDateTime.now());
        return restoConfigRepository.save(config);
    }
}
