package com.caiofilipe.openfinancehub.dto.response;

import com.caiofilipe.openfinancehub.model.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BankAccountResponse {
    private UUID id;
    private String bankName;
    private String accountNumber;
    private String agency;
    private AccountType accountType;
    private BigDecimal balance;
    private String pixKey;
    private LocalDateTime createdAt;
}
