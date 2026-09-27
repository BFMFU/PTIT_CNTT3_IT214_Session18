package ra.edu.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ra.edu.api.entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findBySagaId(String sagaId);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startOfDay AND :endOfDay")
    List<Transaction> findTodayTransactions(LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Transaction> findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(
            Long fromAccountId, Long toAccountId);
}
