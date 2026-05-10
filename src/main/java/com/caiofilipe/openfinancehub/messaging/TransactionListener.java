package com.caiofilipe.openfinancehub.messaging;

import com.caiofilipe.openfinancehub.config.RabbitMQConfig;
import com.caiofilipe.openfinancehub.dto.event.TransactionCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionListener {

    @RabbitListener(queues = RabbitMQConfig.TRANSACTIONS_QUEUE)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        log.info("[TRANSACTION] id={} type={} amount={} account={} user={}",
                event.getTransactionId(),
                event.getType(),
                event.getAmount(),
                event.getBankAccountId(),
                event.getUserId());
    }
}
