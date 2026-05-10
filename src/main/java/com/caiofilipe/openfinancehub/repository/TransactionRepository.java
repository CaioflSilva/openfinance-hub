package com.caiofilipe.openfinancehub.repository;

import com.caiofilipe.openfinancehub.model.Transaction;
import com.caiofilipe.openfinancehub.model.TransactionCategory;
import com.caiofilipe.openfinancehub.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByBankAccountId(UUID bankAccountId);
    List<Transaction> findByBankAccountIdAndStatus(UUID bankAccountId, TransactionStatus status);
    List<Transaction> findByBankAccountIdAndCategory(UUID bankAccountId, TransactionCategory category);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.bankAccount.id = :accountId " +
            "AND t.category = :category AND t.createdAt >= :since")
    BigDecimal sumByAccountIdAndCategoryAndCreatedAtAfter(
            UUID accountId, TransactionCategory category, LocalDateTime since);

    @Query("SELECT t FROM Transaction t WHERE t.bankAccount.user.id = :userId " +
            "AND t.createdAt BETWEEN :start AND :end")
    List<Transaction> findByUserIdAndDateRange(UUID userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.bankAccount.user.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findAllByUserId(UUID userId);
}