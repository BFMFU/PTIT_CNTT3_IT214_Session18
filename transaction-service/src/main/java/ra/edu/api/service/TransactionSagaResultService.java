package ra.edu.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.api.entity.Transaction;
import ra.edu.api.kafka.SagaEvents;
import ra.edu.api.kafka.TransactionEventProducer;
import ra.edu.api.repository.TransactionRepository;

/**
 * Handles Saga result events and updates transaction records.
 * Also publishes compensating events when needed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSagaResultService {

    private final TransactionRepository transactionRepository;
    private final TransactionEventProducer eventProducer;

    @Transactional
    public void markCompleted(SagaEvents.CreditCompletedEvent event) {
        transactionRepository.findBySagaId(event.getSagaId()).ifPresent(tx -> {
            tx.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(tx);

            // Notify success
            eventProducer.notifySuccess(SagaEvents.NotificationEvent.builder()
                    .sagaId(event.getSagaId())
                    .eventType("TRANSFER_SUCCESS")
                    .message(String.format("Transfer of %s completed successfully", event.getAmount()))
                    .amount(event.getAmount())
                    .build());

            log.info("Saga[{}] - Transaction COMPLETED", event.getSagaId());
        });
    }

    @Transactional
    public void markFailedAndCompensate(SagaEvents.CreditFailedEvent event) {
        transactionRepository.findBySagaId(event.getSagaId()).ifPresent(tx -> {
            tx.setStatus(Transaction.TransactionStatus.ROLLED_BACK);
            tx.setFailureReason("Credit failed: " + event.getReason());
            transactionRepository.save(tx);

            // Trigger compensating transaction (refund debit)
            eventProducer.compensateDebit(SagaEvents.CompensateDebitEvent.builder()
                    .sagaId(event.getSagaId())
                    .fromAccountId(event.getFromAccountId())
                    .amount(event.getAmount())
                    .reason(event.getReason())
                    .build());

            // Notify failure
            eventProducer.notifyFailed(SagaEvents.NotificationEvent.builder()
                    .sagaId(event.getSagaId())
                    .eventType("TRANSFER_FAILED")
                    .message("Transfer failed: " + event.getReason())
                    .amount(event.getAmount())
                    .build());

            log.warn("Saga[{}] - Transaction ROLLED_BACK, compensate triggered", event.getSagaId());
        });
    }

    @Transactional
    public void markDebitFailed(SagaEvents.DebitFailedEvent event) {
        transactionRepository.findBySagaId(event.getSagaId()).ifPresent(tx -> {
            tx.setStatus(Transaction.TransactionStatus.FAILED);
            tx.setFailureReason("Debit failed: " + event.getReason());
            transactionRepository.save(tx);

            // Notify failure
            eventProducer.notifyFailed(SagaEvents.NotificationEvent.builder()
                    .sagaId(event.getSagaId())
                    .eventType("TRANSFER_FAILED")
                    .message("Transfer failed: " + event.getReason())
                    .amount(event.getAmount())
                    .build());

            log.warn("Saga[{}] - Transaction FAILED (debit stage)", event.getSagaId());
        });
    }
}
