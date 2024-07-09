package com.atguigu.tingshu.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentRabbitConfig {

    @Bean("paymentExchange")
    public Exchange paymentExchange() {
        return ExchangeBuilder.directExchange("payment_exchange").build();
    }

    @Bean("paymentQueue")
    public Queue paymentQueue() {
        return QueueBuilder.durable("payment_queue").build();
    }

    @Bean
    public Binding orderPayBinding(
            @Qualifier("paymentExchange") Exchange exchange,
            @Qualifier("paymentQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("payment.tingshu").noargs();
    }
}
