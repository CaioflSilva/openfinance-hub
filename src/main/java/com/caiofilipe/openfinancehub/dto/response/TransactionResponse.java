package com.caiofilipe.openfinancehub.dto.response;

import com.caiofilipe.openfinancehub.model.TransactionCategory;
import com.caiofilipe.openfinancehub.model.TransactionStatus;
import com.caiofilipe.openfinancehub.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private BigDecimal amount;
    private String description;
    private TransactionType type;
    private TransactionCategory category;
    private TransactionStatus status;
    private String pixKey;
    private UUID bankAccountId;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
