package com.caiofilipe.openfinancehub.repository;

import com.caiofilipe.openfinancehub.model.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
    List<BankAccount> findByUserId(UUID userId);
    Optional<BankAccount> findByPixKey(String pixKey);
    boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankAccount b WHERE b.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT SUM(b.balance) FROM BankAccount b WHERE b.user.id = :userId")
    Optional<BigDecimal> getTotalBalanceByUserId(UUID userId);
}