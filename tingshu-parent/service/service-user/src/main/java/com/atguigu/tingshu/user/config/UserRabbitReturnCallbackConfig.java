package com.atguigu.tingshu.user.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class UserRabbitReturnCallbackConfig implements RabbitTemplate.ReturnsCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        Message message = returnedMessage.getMessage();
        String s = new String(message.getBody());
        log.info("消息没有抵达消息队列中，交换机为：{}，routingKey为：{}，内容为：{}，错误码为：{}，错误的原因是：{}",
                returnedMessage.getExchange(), returnedMessage.getRoutingKey(), s, returnedMessage.getReplyCode(), returnedMessage.getReplyText());
    }
}
