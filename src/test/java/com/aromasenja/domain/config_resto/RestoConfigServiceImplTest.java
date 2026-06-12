package com.aromasenja.domain.config_resto;

import com.aromasenja.domain.config_resto.dto.RestoConfigResponse;
import com.aromasenja.domain.config_resto.dto.UpdateRestoConfigRequest;
import com.aromasenja.domain.config_resto.entity.RestoConfig;
import com.aromasenja.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestoConfigServiceImpl Unit Tests")
class RestoConfigServiceImplTest {

    @Mock private RestoConfigRepository restoConfigRepository;
    @Mock private ConfigMapper configMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private RestoConfigServiceImpl restoConfigService;

    private RestoConfig config;
    private RestoConfigResponse response;

    @BeforeEach
    void setUp() {
        config = new RestoConfig();
        config.setConfigId(UUID.randomUUID());
        config.setOpen(true);
        config.setOpenTime(LocalTime.of(8, 0));
        config.setCloseTime(LocalTime.of(22, 0));
        config.setNamaRestoran("Aroma Senja");
        config.setTagline("Cita Rasa Nusantara");

        response = new RestoConfigResponse(
                true,
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                "Aroma Senja",
                "Cita Rasa Nusantara",
                "Alamat",
                "0812",
                "email@resto.com",
                "aroma.senja"
        );
    }

    @Test
    @DisplayName("Get Config - Existing Success")
    void getConfig_Existing_Success() {
        when(restoConfigRepository.findFirstBy()).thenReturn(Optional.of(config));
        when(configMapper.toResponse(config)).thenReturn(response);

        RestoConfigResponse result = restoConfigService.getConfig();

        assertThat(result).isNotNull();
        assertThat(result.namaRestoran()).isEqualTo("Aroma Senja");
        verify(restoConfigRepository, times(1)).findFirstBy();
    }

    @Test
    @DisplayName("Get Config - Not Found Create Default")
    void getConfig_NotFound_CreateDefault() {
        when(restoConfigRepository.findFirstBy()).thenReturn(Optional.empty());
        when(restoConfigRepository.save(any(RestoConfig.class))).thenReturn(config);
        when(configMapper.toResponse(any(RestoConfig.class))).thenReturn(response);

        RestoConfigResponse result = restoConfigService.getConfig();

        assertThat(result).isNotNull();
        verify(restoConfigRepository, times(1)).save(any(RestoConfig.class));
    }

    @Test
    @DisplayName("Update Config - Success and Trigger WS")
    void updateConfig_Success_TriggerWS() {
        UpdateRestoConfigRequest request = new UpdateRestoConfigRequest(
                false, // status berubah
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                "New Name",
                "New Tagline",
                "New Alamat",
                "0899",
                "new@email.com",
                "new.ig"
        );

        RestoConfig updatedConfig = new RestoConfig();
        updatedConfig.setOpen(false);

        when(restoConfigRepository.findFirstBy()).thenReturn(Optional.of(config));
        when(restoConfigRepository.save(any(RestoConfig.class))).thenReturn(updatedConfig);
        
        RestoConfigResponse updatedResponse = new RestoConfigResponse(
                false,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                "New Name",
                "New Tagline",
                "New Alamat",
                "0899",
                "new@email.com",
                "new.ig"
        );
        when(configMapper.toResponse(updatedConfig)).thenReturn(updatedResponse);

        RestoConfigResponse result = restoConfigService.updateConfig(request);

        assertThat(result).isNotNull();
        assertThat(result.isOpen()).isFalse();
        verify(notificationService, times(1)).publishRestoStatus(any());
    }
}
