package com.aromasenja.notification;

import com.aromasenja.domain.pesanan.PesananRepository;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import com.aromasenja.notification.payload.PesananStatusWsPayload;
import com.aromasenja.notification.payload.PesananSubscribeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketController Unit Tests")
class WebSocketControllerTest {

    @Mock private PesananRepository pesananRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private WebSocketController webSocketController;

    private UUID pesananId;
    private Pesanan pesanan;

    @BeforeEach
    void setUp() {
        pesananId = UUID.randomUUID();
        pesanan = new Pesanan();
        pesanan.setPesananId(pesananId);
        pesanan.setStatus(StatusPesanan.NEW);
        pesanan.setEstimasiMenit(15);
    }

    @Test
    @DisplayName("Handle Pesanan Subscribe - Found Success")
    void handlePesananSubscribe_Found_Success() {
        PesananSubscribeRequest request = new PesananSubscribeRequest(pesananId);
        when(pesananRepository.findById(pesananId)).thenReturn(Optional.of(pesanan));

        webSocketController.handlePesananSubscribe(request);

        ArgumentCaptor<PesananStatusWsPayload> captor = ArgumentCaptor.forClass(PesananStatusWsPayload.class);
        verify(notificationService, times(1)).publishStatusPesanan(eq(pesananId), captor.capture());

        PesananStatusWsPayload payload = captor.getValue();
        assertThat(payload).isNotNull();
        assertThat(payload.pesananId()).isEqualTo(pesananId);
        assertThat(payload.status()).isEqualTo("NEW");
        assertThat(payload.estimasiMenit()).isEqualTo(15);
    }

    @Test
    @DisplayName("Handle Pesanan Subscribe - Not Found No Action")
    void handlePesananSubscribe_NotFound_NoAction() {
        PesananSubscribeRequest request = new PesananSubscribeRequest(pesananId);
        when(pesananRepository.findById(pesananId)).thenReturn(Optional.empty());

        webSocketController.handlePesananSubscribe(request);

        verify(notificationService, never()).publishStatusPesanan(any(), any());
    }
}
