package com.caiofilipe.openfinancehub.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TRANSACTIONS_QUEUE    = "transactions.created";
    public static final String TRANSACTIONS_EXCHANGE = "transactions.exchange";
    public static final String TRANSACTIONS_ROUTING_KEY = "transactions.created";

    @Bean
    public Queue transactionsCreatedQueue() {
        return QueueBuilder.durable(TRANSACTIONS_QUEUE).build();
    }

    @Bean
    public TopicExchange transactionsExchange() {
        return ExchangeBuilder.topicExchange(TRANSACTIONS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding transactionsBinding(Queue transactionsCreatedQueue, TopicExchange transactionsExchange) {
        return BindingBuilder.bind(transactionsCreatedQueue)
                .to(transactionsExchange)
                .with(TRANSACTIONS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
