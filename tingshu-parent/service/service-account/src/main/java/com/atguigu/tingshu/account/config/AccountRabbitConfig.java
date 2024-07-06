package com.atguigu.tingshu.account.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountRabbitConfig {

    @Bean("accountExchange")
    public Exchange accountExchange() {
        return ExchangeBuilder.fanoutExchange("account_change").build();
    }

    @Bean("orderFanoutQueue")
    public Queue orderFanoutQueue() {
        return QueueBuilder.durable("order_fanout_queue").build();
    }

    @Bean
    public Binding orderFanoutBinding(@Qualifier("accountExchange") Exchange exchange, @Qualifier("orderFanoutQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }

    @Bean("albumFanoutQueue")
    public Queue albumFanoutQueue() {
        return QueueBuilder.durable("album_fanout_queue").build();
    }

    @Bean
    public Binding albumFanoutBinding(@Qualifier("accountExchange") Exchange exchange, @Qualifier("albumFanoutQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }

    @Bean("searchFanoutQueue")
    public Queue searchFanoutQueue() {
        return QueueBuilder.durable("search_fanout_queue").build();
    }

    @Bean
    public Binding searchFanoutBinding(@Qualifier("accountExchange") Exchange exchange, @Qualifier("searchFanoutQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }

    @Bean("userFanoutQueue")
    public Queue userFanoutQueue() {
        return QueueBuilder.durable("user_fanout_queue").build();
    }

    @Bean
    public Binding userFanoutBinding(@Qualifier("accountExchange") Exchange exchange, @Qualifier("userFanoutQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }
}
