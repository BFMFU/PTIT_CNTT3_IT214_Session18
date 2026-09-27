package ra.edu.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.api.entity.Account;
import ra.edu.api.exception.InsufficientFundsException;
import ra.edu.api.exception.ResourceNotFoundException;
import ra.edu.api.kafka.SagaEventProducer;
import ra.edu.api.kafka.SagaEvents;
import ra.edu.api.repository.AccountRepository;

import java.math.BigDecimal;

/**
 * Handles Saga choreography steps for account-service:
 * - Debit (subtract from source account)
 * - Credit (add to destination account)
 * - Compensate Debit (rollback when credit fails)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountSagaService {

    private final AccountRepository accountRepository;
    private final SagaEventProducer sagaEventProducer;

    @Transactional
    public void performDebit(SagaEvents.TransferInitiateEvent event) {
        try {
            Account fromAccount = accountRepository.findByIdWithLock(event.getFromAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + event.getFromAccountId()));

            if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE) {
                throw new IllegalStateException("Source account is not ACTIVE");
            }

            if (fromAccount.getBalance().compareTo(event.getAmount()) < 0) {
                throw new InsufficientFundsException("Insufficient funds. Available: "
                        + fromAccount.getBalance() + ", Required: " + event.getAmount());
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(event.getAmount()));
            accountRepository.save(fromAccount);

            log.info("Saga[{}] - DEBIT SUCCESS: account={}, newBalance={}",
                    event.getSagaId(), event.getFromAccountId(), fromAccount.getBalance());

            // Publish success event → triggers credit step
            sagaEventProducer.publishDebitCompleted(SagaEvents.DebitCompletedEvent.builder()
                    .sagaId(event.getSagaId())
                    .fromAccountId(event.getFromAccountId())
                    .toAccountId(event.getToAccountId())
                    .amount(event.getAmount())
                    .description(event.getDescription())
                    .build());

        } catch (Exception e) {
            log.error("Saga[{}] - DEBIT FAILED: {}", event.getSagaId(), e.getMessage());
            sagaEventProducer.publishDebitFailed(SagaEvents.DebitFailedEvent.builder()
                    .sagaId(event.getSagaId())
                    .fromAccountId(event.getFromAccountId())
                    .amount(event.getAmount())
                    .reason(e.getMessage())
                    .build());
        }
    }

    @Transactional
    public void performCredit(SagaEvents.DebitCompletedEvent event) {
        try {
            Account toAccount = accountRepository.findByIdWithLock(event.getToAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination account not found: " + event.getToAccountId()));

            if (toAccount.getStatus() != Account.AccountStatus.ACTIVE) {
                throw new IllegalStateException("Destination account is not ACTIVE");
            }

            toAccount.setBalance(toAccount.getBalance().add(event.getAmount()));
            accountRepository.save(toAccount);

            log.info("Saga[{}] - CREDIT SUCCESS: account={}, newBalance={}",
                    event.getSagaId(), event.getToAccountId(), toAccount.getBalance());

            // Publish success → saga completes
            sagaEventProducer.publishCreditCompleted(SagaEvents.CreditCompletedEvent.builder()
                    .sagaId(event.getSagaId())
                    .fromAccountId(event.getFromAccountId())
                    .toAccountId(event.getToAccountId())
                    .amount(event.getAmount())
                    .build());

        } catch (Exception e) {
            log.error("Saga[{}] - CREDIT FAILED: {}", event.getSagaId(), e.getMessage());
            // Publish failure → triggers compensating transaction
            sagaEventProducer.publishCreditFailed(SagaEvents.CreditFailedEvent.builder()
                    .sagaId(event.getSagaId())
                    .fromAccountId(event.getFromAccountId())
                    .toAccountId(event.getToAccountId())
                    .amount(event.getAmount())
                    .reason(e.getMessage())
                    .build());
        }
    }

    @Transactional
    public void compensateDebit(SagaEvents.CompensateDebitEvent event) {
        try {
            Account fromAccount = accountRepository.findByIdWithLock(event.getFromAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found for compensation: " + event.getFromAccountId()));

            // Refund: add back the debited amount
            fromAccount.setBalance(fromAccount.getBalance().add(event.getAmount()));
            accountRepository.save(fromAccount);

            log.warn("Saga[{}] - COMPENSATE_DEBIT SUCCESS: account={}, refundedAmount={}, newBalance={}",
                    event.getSagaId(), event.getFromAccountId(), event.getAmount(), fromAccount.getBalance());

        } catch (Exception e) {
            log.error("Saga[{}] - COMPENSATE_DEBIT FAILED (CRITICAL): {}", event.getSagaId(), e.getMessage());
            // In production: alert DLQ / manual intervention required
        }
    }
}
