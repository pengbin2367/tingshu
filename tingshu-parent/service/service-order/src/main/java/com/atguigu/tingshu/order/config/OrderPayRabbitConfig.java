package com.atguigu.tingshu.order.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderPayRabbitConfig {

    @Bean("orderPayExchange")
    public Exchange orderPayExchange() {
        return ExchangeBuilder.directExchange("order_pay_change").build();
    }

    @Bean("orderPayQueue")
    public Queue orderPayQueue() {
        return QueueBuilder.durable("order_pay_queue").build();
    }

    @Bean
    public Binding orderPayBinding(
            @Qualifier("orderPayExchange") Exchange exchange,
            @Qualifier("orderPayQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("order.pay").noargs();
    }
}
