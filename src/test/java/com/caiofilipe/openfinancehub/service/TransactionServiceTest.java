package com.caiofilipe.openfinancehub.service;

import com.caiofilipe.openfinancehub.config.RabbitMQConfig;
import com.caiofilipe.openfinancehub.dto.event.TransactionCreatedEvent;
import com.caiofilipe.openfinancehub.dto.request.TransactionRequest;
import com.caiofilipe.openfinancehub.exception.BusinessException;
import com.caiofilipe.openfinancehub.model.*;
import com.caiofilipe.openfinancehub.repository.BankAccountRepository;
import com.caiofilipe.openfinancehub.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock BankAccountRepository bankAccountRepository;
    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks TransactionService transactionService;

    private User user;
    private BankAccount account;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        user = User.builder().id(userId).name("User").email("user@test.com")
                .password("pass").role(Role.USER).build();

        account = BankAccount.builder()
                .id(accountId)
                .bankName("Bank")
                .accountNumber("123")
                .agency("001")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("500.00"))
                .user(user)
                .build();
    }

    private TransactionRequest buildDebitRequest(BigDecimal amount) {
        TransactionRequest req = new TransactionRequest();
        req.setBankAccountId(accountId);
        req.setAmount(amount);
        req.setDescription("Test debit");
        req.setType(TransactionType.DEBIT);
        req.setCategory(TransactionCategory.OTHER);
        return req;
    }

    @Test
    void create_debit_deductsBalanceCorrectly() {
        when(bankAccountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(bankAccountRepository.save(any())).thenReturn(account);
        when(transactionRepository.save(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setBankAccount(account);
            return t;
        });

        transactionService.create(buildDebitRequest(new BigDecimal("100.00")), user);

        ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo("400.00");
    }

    @Test
    void create_insufficientBalance_throwsBusinessException() {
        when(bankAccountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.create(buildDebitRequest(new BigDecimal("999.00")), user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void create_publishesEventToRabbitMQ() {
        when(bankAccountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(bankAccountRepository.save(any())).thenReturn(account);
        when(transactionRepository.save(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setBankAccount(account);
            return t;
        });

        transactionService.create(buildDebitRequest(new BigDecimal("50.00")), user);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.TRANSACTIONS_EXCHANGE),
                eq(RabbitMQConfig.TRANSACTIONS_ROUTING_KEY),
                any(TransactionCreatedEvent.class)
        );
    }
}
