package ra.edu.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ra.edu.api.entity.Transaction;
import ra.edu.api.kafka.SagaEvents;
import ra.edu.api.kafka.TransactionEventProducer;
import ra.edu.api.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionSagaResultService Unit Tests")
class TransactionSagaResultServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEventProducer eventProducer;

    @InjectMocks
    private TransactionSagaResultService sagaResultService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
                .id(1L)
                .sagaId("test-saga-id-123")
                .transactionType(Transaction.TransactionType.TRANSFER)
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("500000.00"))
                .status(Transaction.TransactionStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("markCompleted should set status to COMPLETED and publish notification")
    void markCompleted_ShouldSetStatusCompleted() {
        when(transactionRepository.findBySagaId("test-saga-id-123")).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        SagaEvents.CreditCompletedEvent event = SagaEvents.CreditCompletedEvent.builder()
                .sagaId("test-saga-id-123")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("500000.00"))
                .build();

        sagaResultService.markCompleted(event);

        verify(transactionRepository).save(argThat(tx ->
                tx.getStatus() == Transaction.TransactionStatus.COMPLETED));
        verify(eventProducer).notifySuccess(any(SagaEvents.NotificationEvent.class));
    }

    @Test
    @DisplayName("markFailedAndCompensate should set status to ROLLED_BACK and trigger compensate")
    void markFailedAndCompensate_ShouldTriggerCompensate() {
        when(transactionRepository.findBySagaId("test-saga-id-123")).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        SagaEvents.CreditFailedEvent event = SagaEvents.CreditFailedEvent.builder()
                .sagaId("test-saga-id-123")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("500000.00"))
                .reason("Account suspended")
                .build();

        sagaResultService.markFailedAndCompensate(event);

        verify(transactionRepository).save(argThat(tx ->
                tx.getStatus() == Transaction.TransactionStatus.ROLLED_BACK));
        verify(eventProducer).compensateDebit(any(SagaEvents.CompensateDebitEvent.class));
        verify(eventProducer).notifyFailed(any(SagaEvents.NotificationEvent.class));
    }
}
