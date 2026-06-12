package com.aromasenja.notification;

import com.aromasenja.domain.pesanan.PesananRepository;
import com.aromasenja.notification.payload.PesananStatusWsPayload;
import com.aromasenja.notification.payload.PesananSubscribeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final PesananRepository pesananRepository;
    private final NotificationService notificationService;

    @MessageMapping("/pesanan/subscribe")
    public void handlePesananSubscribe(PesananSubscribeRequest request) {
        log.info("Received subscribe request for pesananId: {}", request.pesananId());

        if (request.pesananId() != null) {
            pesananRepository.findById(request.pesananId()).ifPresent(pesanan -> {
                PesananStatusWsPayload payload = new PesananStatusWsPayload(
                        pesanan.getPesananId(),
                        pesanan.getStatus().name(),
                        pesanan.getEstimasiMenit(),
                        LocalDateTime.now()
                );
                notificationService.publishStatusPesanan(pesanan.getPesananId(), payload);
                log.info("Sent initial status for pesananId {} to /topic/pesanan/{}", 
                        pesanan.getPesananId(), pesanan.getPesananId());
            });
        }
    }
}
