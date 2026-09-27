package ra.edu.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ra.edu.api.service.TransactionSagaResultService;

import java.util.Map;

/**
 * Consumes Saga result events from account-service
 * and updates Transaction records accordingly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaResultConsumer {

    private final TransactionSagaResultService sagaResultService;
    private final ObjectMapper objectMapper;

    /**
     * Credit was successful → Transaction = COMPLETED
     */
    @KafkaListener(topics = KafkaTopics.SAGA_CREDIT_COMPLETED,
            groupId = "transaction-saga-group",
            containerFactory = "sagaKafkaListenerContainerFactory")
    public void onCreditCompleted(Map<String, Object> payload) {
        try {
            SagaEvents.CreditCompletedEvent event = objectMapper.convertValue(payload, SagaEvents.CreditCompletedEvent.class);
            log.info("Saga[{}] - Received CREDIT_COMPLETED → marking COMPLETED", event.getSagaId());
            sagaResultService.markCompleted(event);
        } catch (Exception e) {
            log.error("Failed to process CREDIT_COMPLETED: {}", e.getMessage(), e);
        }
    }

    /**
     * Credit failed → trigger compensate, Transaction = ROLLED_BACK
     */
    @KafkaListener(topics = KafkaTopics.SAGA_CREDIT_FAILED,
            groupId = "transaction-saga-group",
            containerFactory = "sagaKafkaListenerContainerFactory")
    public void onCreditFailed(Map<String, Object> payload) {
        try {
            SagaEvents.CreditFailedEvent event = objectMapper.convertValue(payload, SagaEvents.CreditFailedEvent.class);
            log.warn("Saga[{}] - Received CREDIT_FAILED → triggering compensate", event.getSagaId());
            sagaResultService.markFailedAndCompensate(event);
        } catch (Exception e) {
            log.error("Failed to process CREDIT_FAILED: {}", e.getMessage(), e);
        }
    }

    /**
     * Debit failed → Transaction = FAILED (no compensation needed, money was not moved)
     */
    @KafkaListener(topics = KafkaTopics.SAGA_DEBIT_FAILED,
            groupId = "transaction-saga-group",
            containerFactory = "sagaKafkaListenerContainerFactory")
    public void onDebitFailed(Map<String, Object> payload) {
        try {
            SagaEvents.DebitFailedEvent event = objectMapper.convertValue(payload, SagaEvents.DebitFailedEvent.class);
            log.warn("Saga[{}] - Received DEBIT_FAILED → marking FAILED", event.getSagaId());
            sagaResultService.markDebitFailed(event);
        } catch (Exception e) {
            log.error("Failed to process DEBIT_FAILED: {}", e.getMessage(), e);
        }
    }
}
