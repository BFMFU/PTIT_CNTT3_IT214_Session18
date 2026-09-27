package ra.edu.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ra.edu.api.service.AccountSagaService;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventConsumer {

    private final AccountSagaService accountSagaService;
    private final ObjectMapper objectMapper;

    /**
     * Step 1: Receive transfer initiate → perform DEBIT on fromAccount
     */
    @KafkaListener(topics = KafkaTopics.SAGA_TRANSFER_INITIATE, groupId = "account-saga-group",
            containerFactory = "sagaKafkaListenerContainerFactory")
    public void onTransferInitiate(Map<String, Object> payload) {
        try {
            SagaEvents.TransferInitiateEvent event = objectMapper.convertValue(payload, SagaEvents.TransferInitiateEvent.class);
            log.info("Saga[{}] - Received TRANSFER_INITIATE: from={}, to={}, amount={}",
                    event.getSagaId(), event.getFromAccountId(), event.getToAccountId(), event.getAmount());
            accountSagaService.performDebit(event);
        } catch (Exception e) {
            log.error("Failed to process TRANSFER_INITIATE event: {}", e.getMessage(), e);
        }
    }

    /**
     * Step 2: After debit completed → perform CREDIT on toAccount
     */
    @KafkaListener(topics = KafkaTopics.SAGA_DEBIT_COMPLETED, groupId = "account-saga-group",
            containerFactory = "sagaKafkaListenerContainerFactory")
    public void onDebitCompleted(Map<String, Object> payload) {
        try {
            SagaEvents.DebitCompletedEvent event = objectMapper.convertValue(payload, SagaEvents.DebitCompletedEvent.class);
            log.info("Saga[{}] - Received DEBIT_COMPLETED, performing credit on account {}",
                    event.getSagaId(), event.getToAccountId());
            accountSagaService.performCredit(event);
        } catch (Exception e) {
            log.error("Failed to process DEBIT_COMPLETED event: {}", e.getMessage(), e);
        }
    }

    /**
     * Compensating: Rollback debit when credit fails
     */
    @KafkaListener(topics = KafkaTopics.SAGA_COMPENSATE_DEBIT, groupId = "account-saga-group",
            containerFactory = "sagaKafkaListenerContainerFactory")
    public void onCompensateDebit(Map<String, Object> payload) {
        try {
            SagaEvents.CompensateDebitEvent event = objectMapper.convertValue(payload, SagaEvents.CompensateDebitEvent.class);
            log.warn("Saga[{}] - Received COMPENSATE_DEBIT for account {}, amount={}",
                    event.getSagaId(), event.getFromAccountId(), event.getAmount());
            accountSagaService.compensateDebit(event);
        } catch (Exception e) {
            log.error("Failed to process COMPENSATE_DEBIT event: {}", e.getMessage(), e);
        }
    }
}
