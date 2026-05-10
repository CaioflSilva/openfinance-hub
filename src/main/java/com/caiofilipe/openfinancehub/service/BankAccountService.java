package com.caiofilipe.openfinancehub.service;

import com.caiofilipe.openfinancehub.dto.request.BankAccountRequest;
import com.caiofilipe.openfinancehub.dto.response.BankAccountResponse;
import com.caiofilipe.openfinancehub.exception.ForbiddenException;
import com.caiofilipe.openfinancehub.exception.ResourceNotFoundException;
import com.caiofilipe.openfinancehub.model.BankAccount;
import com.caiofilipe.openfinancehub.model.User;
import com.caiofilipe.openfinancehub.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    public BankAccountResponse create(BankAccountRequest request, User user) {
        if (bankAccountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new RuntimeException("Número de conta já cadastrado");
        }

        BankAccount account = BankAccount.builder()
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .agency(request.getAgency())
                .accountType(request.getAccountType())
                .balance(request.getBalance())
                .pixKey(request.getPixKey())
                .user(user)
                .build();

        return toResponse(bankAccountRepository.save(account));
    }

    public List<BankAccountResponse> listByUser(User user) {
        return bankAccountRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BankAccountResponse getById(UUID id, User user) {
        BankAccount account = findAndValidateOwnership(id, user);
        return toResponse(account);
    }

    public void delete(UUID id, User user) {
        findAndValidateOwnership(id, user);
        bankAccountRepository.deleteById(id);
    }

    private BankAccount findAndValidateOwnership(UUID id, User user) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta bancária não encontrada"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Acesso negado a esta conta bancária");
        }

        return account;
    }

    private BankAccountResponse toResponse(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getId())
                .bankName(account.getBankName())
                .accountNumber(account.getAccountNumber())
                .agency(account.getAgency())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .pixKey(account.getPixKey())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
