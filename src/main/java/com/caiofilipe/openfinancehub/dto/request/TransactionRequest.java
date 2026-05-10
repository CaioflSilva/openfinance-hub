package com.caiofilipe.openfinancehub.dto.request;

import com.caiofilipe.openfinancehub.model.TransactionCategory;
import com.caiofilipe.openfinancehub.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransactionRequest {

    @NotNull(message = "ID da conta bancária é obrigatório")
    private UUID bankAccountId;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull(message = "Tipo de transação é obrigatório")
    private TransactionType type;

    @NotNull(message = "Categoria é obrigatória")
    private TransactionCategory category;

    private String pixKey;
}
