package com.caiofilipe.openfinancehub.service;

import com.caiofilipe.openfinancehub.dto.response.DashboardSummaryResponse;
import com.caiofilipe.openfinancehub.dto.response.TransactionResponse;
import com.caiofilipe.openfinancehub.model.Transaction;
import com.caiofilipe.openfinancehub.model.TransactionCategory;
import com.caiofilipe.openfinancehub.model.TransactionType;
import com.caiofilipe.openfinancehub.model.User;
import com.caiofilipe.openfinancehub.repository.BankAccountRepository;
import com.caiofilipe.openfinancehub.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final List<TransactionType> INCOME_TYPES =
            List.of(TransactionType.CREDIT, TransactionType.PIX_IN);

    private static final List<TransactionType> EXPENSE_TYPES =
            List.of(TransactionType.DEBIT, TransactionType.PIX_OUT, TransactionType.TRANSFER);

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    @Cacheable(value = "dashboard-summary", key = "#user.id.toString()")
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(User user) {
        LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        BigDecimal totalBalance = bankAccountRepository
                .getTotalBalanceByUserId(user.getId())
                .orElse(BigDecimal.ZERO);

        BigDecimal monthlyIncome = coalesce(
                transactionRepository.sumByUserIdAndTypesAndDateRange(user.getId(), INCOME_TYPES, start, end));

        BigDecimal monthlyExpenses = coalesce(
                transactionRepository.sumByUserIdAndTypesAndDateRange(user.getId(), EXPENSE_TYPES, start, end));

        Map<String, BigDecimal> balanceByCategory = new LinkedHashMap<>();
        transactionRepository.sumByCategoryForUser(user.getId(), start, end)
                .forEach(row -> balanceByCategory.put(
                        ((TransactionCategory) row[0]).name(),
                        (BigDecimal) row[1]));

        List<TransactionResponse> topTransactions = transactionRepository
                .findTopByUserId(user.getId(), PageRequest.of(0, 5))
                .stream()
                .map(this::toResponse)
                .toList();

        return DashboardSummaryResponse.builder()
                .totalBalance(totalBalance)
                .monthlyIncome(monthlyIncome)
                .monthlyExpenses(monthlyExpenses)
                .balanceByCategory(balanceByCategory)
                .topTransactions(topTransactions)
                .build();
    }

    private BigDecimal coalesce(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .description(t.getDescription())
                .type(t.getType())
                .category(t.getCategory())
                .status(t.getStatus())
                .pixKey(t.getPixKey())
                .bankAccountId(t.getBankAccount().getId())
                .createdAt(t.getCreatedAt())
                .processedAt(t.getProcessedAt())
                .build();
    }
}
