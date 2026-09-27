package ra.edu.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Notification service Kafka consumer.
 * Listens to transfer success/failed events and processes notifications.
 * In production: send email/SMS/push notification.
 */
@Component
@Slf4j
public class NotificationEventConsumer {

    private static final String SUCCESS_TOPIC = "notification.transfer.success";
    private static final String FAILED_TOPIC = "notification.transfer.failed";

    /**
     * Receives successful transfer notification from transaction-service via Kafka.
     * Demonstrates Event-Driven Architecture / Async communication.
     */
    @KafkaListener(topics = SUCCESS_TOPIC, groupId = "notification-group",
            containerFactory = "notificationKafkaListenerContainerFactory")
    public void onTransferSuccess(NotificationEvents.NotificationEvent event) {
        log.info("=== [NOTIFICATION] ====================================");
        log.info("TRANSFER SUCCESS — Saga: {}", event.getSagaId());
        log.info("Message: {}", event.getMessage());
        log.info("Amount: {}", event.getAmount());
        log.info("=======================================================");
        // TODO: Send push notification / email to customer
        sendNotification(event);
    }

    /**
     * Receives failed transfer notification from transaction-service via Kafka.
     */
    @KafkaListener(topics = FAILED_TOPIC, groupId = "notification-group",
            containerFactory = "notificationKafkaListenerContainerFactory")
    public void onTransferFailed(NotificationEvents.NotificationEvent event) {
        log.warn("=== [NOTIFICATION] ====================================");
        log.warn("TRANSFER FAILED — Saga: {}", event.getSagaId());
        log.warn("Message: {}", event.getMessage());
        log.warn("Amount: {}", event.getAmount());
        log.warn("=======================================================");
        sendNotification(event);
    }

    private void sendNotification(NotificationEvents.NotificationEvent event) {
        // Simulated notification dispatch
        log.info("[NotificationService] Dispatching '{}' notification for customer: {}",
                event.getEventType(), event.getCustomerId());
        // In real implementation: EmailService.send() / SMSService.send() / FCMService.push()
    }
}
