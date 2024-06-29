package com.atguigu.tingshu.user.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRabbitConfig {

    @Bean("userExchange")
    public Exchange userExchange() {
        return ExchangeBuilder.directExchange("user_exchange").build();
    }

    @Bean("userQueue")
    public Queue userQueue() {
        return QueueBuilder.durable("user_queue").build();
    }

    @Bean
    public Binding userBinding(@Qualifier("userExchange") Exchange exchange, @Qualifier("userQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("user.account").noargs();
    }
}
