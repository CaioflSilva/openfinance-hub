package com.caiofilipe.openfinancehub.service;

import com.caiofilipe.openfinancehub.dto.request.BankAccountRequest;
import com.caiofilipe.openfinancehub.dto.response.BankAccountResponse;
import com.caiofilipe.openfinancehub.exception.ForbiddenException;
import com.caiofilipe.openfinancehub.exception.ResourceNotFoundException;
import com.caiofilipe.openfinancehub.model.AccountType;
import com.caiofilipe.openfinancehub.model.BankAccount;
import com.caiofilipe.openfinancehub.model.Role;
import com.caiofilipe.openfinancehub.model.User;
import com.caiofilipe.openfinancehub.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock BankAccountRepository bankAccountRepository;
    @InjectMocks BankAccountService bankAccountService;

    private User owner;
    private User otherUser;
    private BankAccount account;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        UUID ownerId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        owner = User.builder().id(ownerId).name("Owner").email("owner@test.com")
                .password("pass").role(Role.USER).build();

        otherUser = User.builder().id(UUID.randomUUID()).name("Other").email("other@test.com")
                .password("pass").role(Role.USER).build();

        account = BankAccount.builder()
                .id(accountId)
                .bankName("Test Bank")
                .accountNumber("12345")
                .agency("001")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .user(owner)
                .build();
    }

    @Test
    void create_success_returnsMappedResponse() {
        BankAccountRequest request = new BankAccountRequest();
        request.setBankName("Nubank");
        request.setAccountNumber("99999");
        request.setAgency("0001");
        request.setAccountType(AccountType.CHECKING);
        request.setBalance(new BigDecimal("500.00"));

        when(bankAccountRepository.existsByAccountNumber("99999")).thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        BankAccountResponse response = bankAccountService.create(request, owner);

        assertThat(response.getBankName()).isEqualTo("Nubank");
        assertThat(response.getBalance()).isEqualByComparingTo("500.00");
        assertThat(response.getAccountNumber()).isEqualTo("99999");
    }

    @Test
    void getById_ownerAccessing_returnsAccount() {
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BankAccountResponse response = bankAccountService.getById(accountId, owner);

        assertThat(response.getId()).isEqualTo(accountId);
        assertThat(response.getBankName()).isEqualTo("Test Bank");
    }

    @Test
    void getById_differentUser_throwsForbiddenException() {
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> bankAccountService.getById(accountId, otherUser))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Acesso negado");
    }

    @Test
    void getById_nonExistent_throwsResourceNotFoundException() {
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.getById(accountId, owner))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
