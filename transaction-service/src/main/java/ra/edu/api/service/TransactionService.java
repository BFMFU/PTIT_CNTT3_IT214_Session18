package ra.edu.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.api.dto.TransferRequest;
import ra.edu.api.entity.Transaction;
import ra.edu.api.kafka.SagaEvents;
import ra.edu.api.kafka.TransactionEventProducer;
import ra.edu.api.repository.TransactionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionEventProducer eventProducer;

    /**
     * CUSTOMER initiates a transfer → creates PENDING transaction → publishes Saga initiate event
     */
    @Transactional
    public Transaction initiateTransfer(TransferRequest request, Long customerId) {
        String sagaId = UUID.randomUUID().toString();

        Transaction transaction = Transaction.builder()
                .sagaId(sagaId)
                .transactionType(Transaction.TransactionType.TRANSFER)
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction created: sagaId={}, from={}, to={}, amount={}",
                sagaId, request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        // Kick off Saga choreography
        eventProducer.initiateTransfer(SagaEvents.TransferInitiateEvent.builder()
                .sagaId(sagaId)
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build());

        return transaction;
    }

    /**
     * TELLER views today's transactions
     */
    public List<Transaction> findTodayTransactions() {
        LocalDate today = LocalDate.now();
        return transactionRepository.findTodayTransactions(
                today.atStartOfDay(),
                today.atTime(LocalTime.MAX)
        );
    }

    /**
     * CUSTOMER views own transaction history
     */
    public List<Transaction> findMyTransactions(Long customerId, List<Long> accountIds) {
        // Find transactions where any of the customer's accounts are involved
        return transactionRepository.findAll().stream()
                .filter(t -> (t.getFromAccountId() != null && accountIds.contains(t.getFromAccountId()))
                        || (t.getToAccountId() != null && accountIds.contains(t.getToAccountId())))
                .toList();
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }
}
