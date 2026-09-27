package ra.edu.api.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Initiate the Saga by publishing the first event
     */
    public void initiateTransfer(SagaEvents.TransferInitiateEvent event) {
        kafkaTemplate.send(KafkaTopics.SAGA_TRANSFER_INITIATE, event.getSagaId(), event);
        log.info("Saga[{}] - Published TRANSFER_INITIATE", event.getSagaId());
    }

    /**
     * Publish compensating transaction event (rollback debit)
     */
    public void compensateDebit(SagaEvents.CompensateDebitEvent event) {
        kafkaTemplate.send(KafkaTopics.SAGA_COMPENSATE_DEBIT, event.getSagaId(), event);
        log.warn("Saga[{}] - Published COMPENSATE_DEBIT", event.getSagaId());
    }

    /**
     * Notify notification-service about successful transfer
     */
    public void notifySuccess(SagaEvents.NotificationEvent event) {
        kafkaTemplate.send(KafkaTopics.NOTIFICATION_TRANSFER_SUCCESS, event.getSagaId(), event);
        log.info("Saga[{}] - Published NOTIFICATION_SUCCESS", event.getSagaId());
    }

    /**
     * Notify notification-service about failed transfer
     */
    public void notifyFailed(SagaEvents.NotificationEvent event) {
        kafkaTemplate.send(KafkaTopics.NOTIFICATION_TRANSFER_FAILED, event.getSagaId(), event);
        log.warn("Saga[{}] - Published NOTIFICATION_FAILED", event.getSagaId());
    }
}
