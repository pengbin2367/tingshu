package com.atguigu.tingshu.order.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderInfoDelayRabbitConfig {

    @Bean("orderNormalExchange")
    public Exchange orderNormalExchange() {
        return ExchangeBuilder.directExchange("order_normal_change").build();
    }

    @Bean("orderDeadQueue")
    public Queue orderDeadQueue() {
        return QueueBuilder.durable("order_dead_queue")
                .withArgument("x-dead-letter-exchange", "order_dead_change")
                .withArgument("x-dead-letter-routing-key", "order.normal")
                .build();
    }

    @Bean
    public Binding orderNormalBinding(
            @Qualifier("orderNormalExchange") Exchange exchange,
            @Qualifier("orderDeadQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("order.dead").noargs();
    }

    @Bean("orderDeadExchange")
    public Exchange orderDeadExchange() {
        return ExchangeBuilder.directExchange("order_dead_change").build();
    }

    @Bean("orderNormalQueue")
    public Queue orderNormalQueue() {
        return QueueBuilder.durable("order_normal_queue").build();
    }

    @Bean
    public Binding orderDeadBinding(
            @Qualifier("orderDeadExchange") Exchange exchange,
            @Qualifier("orderNormalQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("order.normal").noargs();
    }
}
