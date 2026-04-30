package com.example.notification_service.consumer;

import com.example.notification_service.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @KafkaListener(topics = "notificationTopic", groupId = "notification-group")
    public void handleNotification(OrderPlacedEvent orderPlacedEvent) {
        log.info("Email Notification sent for Order: {}", orderPlacedEvent.getOrderNumber());
        // Di sini biasanya ada logika untuk memanggil email API (SendGrid/Mailchimp)
    }
}
