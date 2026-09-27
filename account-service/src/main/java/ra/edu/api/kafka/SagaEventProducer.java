package ra.edu.api.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDebitCompleted(SagaEvents.DebitCompletedEvent event) {
        kafkaTemplate.send(KafkaTopics.SAGA_DEBIT_COMPLETED, event.getSagaId(), event);
        log.info("Saga[{}] - Published DEBIT_COMPLETED", event.getSagaId());
    }

    public void publishDebitFailed(SagaEvents.DebitFailedEvent event) {
        kafkaTemplate.send(KafkaTopics.SAGA_DEBIT_FAILED, event.getSagaId(), event);
        log.warn("Saga[{}] - Published DEBIT_FAILED: {}", event.getSagaId(), event.getReason());
    }

    public void publishCreditCompleted(SagaEvents.CreditCompletedEvent event) {
        kafkaTemplate.send(KafkaTopics.SAGA_CREDIT_COMPLETED, event.getSagaId(), event);
        log.info("Saga[{}] - Published CREDIT_COMPLETED", event.getSagaId());
    }

    public void publishCreditFailed(SagaEvents.CreditFailedEvent event) {
        kafkaTemplate.send(KafkaTopics.SAGA_CREDIT_FAILED, event.getSagaId(), event);
        log.warn("Saga[{}] - Published CREDIT_FAILED: {}", event.getSagaId(), event.getReason());
    }
}
