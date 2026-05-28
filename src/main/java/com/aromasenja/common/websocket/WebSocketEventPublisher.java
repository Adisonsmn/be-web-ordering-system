package com.aromasenja.common.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper di atas SimpMessagingTemplate.
 *
 * ATURAN: Ini adalah SATU-SATUNYA class di seluruh codebase yang boleh
 * menggunakan SimpMessagingTemplate secara langsung.
 * Domain services (PesananServiceImpl, dll.) HARUS menggunakan
 * NotificationService, bukan inject SimpMessagingTemplate langsung.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Publish event ke STOMP topic.
     *
     * @param destination STOMP destination, contoh: "/topic/admin/pesanan-baru"
     * @param payload     object yang akan di-serialize ke JSON
     */
    public void publish(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("WebSocket event published ke destination: {}", destination);
    }
}
