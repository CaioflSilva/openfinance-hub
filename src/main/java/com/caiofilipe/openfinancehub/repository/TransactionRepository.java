package com.caiofilipe.openfinancehub.repository;

import com.caiofilipe.openfinancehub.model.Transaction;
import com.caiofilipe.openfinancehub.model.TransactionCategory;
import com.caiofilipe.openfinancehub.model.TransactionStatus;
import com.caiofilipe.openfinancehub.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByBankAccountId(UUID bankAccountId, Pageable pageable);
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
    Page<Transaction> findAllByUserId(UUID userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.bankAccount.user.id = :userId AND t.type IN :types " +
            "AND t.createdAt >= :start AND t.createdAt < :end")
    BigDecimal sumByUserIdAndTypesAndDateRange(UUID userId, List<TransactionType> types,
            LocalDateTime start, LocalDateTime end);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t " +
            "WHERE t.bankAccount.user.id = :userId " +
            "AND t.createdAt >= :start AND t.createdAt < :end " +
            "GROUP BY t.category")
    List<Object[]> sumByCategoryForUser(UUID userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.bankAccount.user.id = :userId ORDER BY t.amount DESC")
    List<Transaction> findTopByUserId(UUID userId, Pageable pageable);
}