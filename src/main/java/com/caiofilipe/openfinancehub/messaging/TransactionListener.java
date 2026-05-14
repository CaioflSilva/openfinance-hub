package com.caiofilipe.openfinancehub.messaging;

import com.caiofilipe.openfinancehub.config.RabbitMQConfig;
import com.caiofilipe.openfinancehub.dto.event.TransactionCreatedEvent;
import com.caiofilipe.openfinancehub.model.TransactionStatus;
import com.caiofilipe.openfinancehub.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionListener {

    private final TransactionRepository transactionRepository;

    @RabbitListener(queues = RabbitMQConfig.TRANSACTIONS_QUEUE)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        log.info("[TRANSACTION] id={} type={} amount={} account={} user={}",
                event.getTransactionId(),
                event.getType(),
                event.getAmount(),
                event.getBankAccountId(),
                event.getUserId());

        transactionRepository.findById(event.getTransactionId()).ifPresentOrElse(
                transaction -> {
                    transaction.setStatus(TransactionStatus.COMPLETED);
                    transaction.setProcessedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                    log.info("[TRANSACTION] id={} marked as COMPLETED", event.getTransactionId());
                },
                () -> log.warn("[TRANSACTION] id={} not found, skipping status update",
                        event.getTransactionId())
        );
    }
}
