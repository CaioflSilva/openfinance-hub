package com.caiofilipe.openfinancehub.service;

import com.caiofilipe.openfinancehub.config.RabbitMQConfig;
import com.caiofilipe.openfinancehub.dto.event.TransactionCreatedEvent;
import com.caiofilipe.openfinancehub.dto.request.TransactionRequest;
import com.caiofilipe.openfinancehub.dto.response.TransactionResponse;
import com.caiofilipe.openfinancehub.exception.BusinessException;
import com.caiofilipe.openfinancehub.exception.ForbiddenException;
import com.caiofilipe.openfinancehub.exception.ResourceNotFoundException;
import com.caiofilipe.openfinancehub.model.BankAccount;
import com.caiofilipe.openfinancehub.model.Transaction;
import com.caiofilipe.openfinancehub.model.TransactionStatus;
import com.caiofilipe.openfinancehub.model.TransactionType;
import com.caiofilipe.openfinancehub.model.User;
import com.caiofilipe.openfinancehub.repository.BankAccountRepository;
import com.caiofilipe.openfinancehub.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @CacheEvict(value = "dashboard-summary", key = "#user.id.toString()")
    public TransactionResponse create(TransactionRequest request, User user) {
        BankAccount account = bankAccountRepository.findByIdForUpdate(request.getBankAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Acesso negado a esta conta bancária");
        }

        updateBalance(account, request);
        bankAccountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .type(request.getType())
                .category(request.getCategory())
                .status(TransactionStatus.PENDING)
                .pixKey(request.getPixKey())
                .bankAccount(account)
                .build();

        Transaction saved = transactionRepository.save(transaction);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TRANSACTIONS_EXCHANGE,
                RabbitMQConfig.TRANSACTIONS_ROUTING_KEY,
                TransactionCreatedEvent.builder()
                        .transactionId(saved.getId())
                        .bankAccountId(account.getId())
                        .userId(user.getId())
                        .amount(saved.getAmount())
                        .type(saved.getType())
                        .category(saved.getCategory())
                        .description(saved.getDescription())
                        .createdAt(saved.getCreatedAt())
                        .build()
        );

        return toResponse(saved);
    }

    public Page<TransactionResponse> listByAccount(UUID accountId, User user, Pageable pageable) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Acesso negado a esta conta bancária");
        }

        return transactionRepository.findByBankAccountId(accountId, pageable)
                .map(this::toResponse);
    }

    public Page<TransactionResponse> listByUser(User user, Pageable pageable) {
        return transactionRepository.findAllByUserId(user.getId(), pageable)
                .map(this::toResponse);
    }

    public TransactionResponse getById(UUID id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));

        if (!transaction.getBankAccount().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Acesso negado a esta transação");
        }

        return toResponse(transaction);
    }

    private void updateBalance(BankAccount account, TransactionRequest request) {
        if (request.getType() == TransactionType.CREDIT || request.getType() == TransactionType.PIX_IN) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else {
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new BusinessException("Saldo insuficiente para realizar a transação");
            }
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        }
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
