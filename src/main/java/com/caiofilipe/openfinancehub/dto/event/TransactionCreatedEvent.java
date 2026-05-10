package com.caiofilipe.openfinancehub.dto.event;

import com.caiofilipe.openfinancehub.model.TransactionCategory;
import com.caiofilipe.openfinancehub.model.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionCreatedEvent {
    private UUID transactionId;
    private UUID bankAccountId;
    private UUID userId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionCategory category;
    private String description;
    private LocalDateTime createdAt;
}
