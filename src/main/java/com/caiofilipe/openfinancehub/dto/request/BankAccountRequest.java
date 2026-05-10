package com.caiofilipe.openfinancehub.dto.request;

import com.caiofilipe.openfinancehub.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankAccountRequest {

    @NotBlank(message = "Nome do banco é obrigatório")
    private String bankName;

    @NotBlank(message = "Número da conta é obrigatório")
    private String accountNumber;

    @NotBlank(message = "Agência é obrigatória")
    private String agency;

    @NotNull(message = "Tipo de conta é obrigatório")
    private AccountType accountType;

    @NotNull(message = "Saldo inicial é obrigatório")
    @DecimalMin(value = "0.00", message = "Saldo não pode ser negativo")
    private BigDecimal balance;

    private String pixKey;
}
