package com.atguigu.tingshu.account.consumer;

import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.model.account.UserAccount;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
@Component
public class AccountInitConsumer {

    @Autowired
    private UserAccountService userAccountService;

    @RabbitListener(queues = "user_queue")
    public void accountInitConsumer(Channel channel, Message message) {
        MessageProperties messageProperties = message.getMessageProperties();
        // 消息编号
        long deliveryTag = messageProperties.getDeliveryTag();
        String msg = new String(message.getBody());
        log.info("account收到消息编号为：{}的消息内容：{}", deliveryTag, msg);
        try {
            // 确认消息，false=只确认当前的消息
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("消息【{}】消费失败", deliveryTag);
            try {
                if (messageProperties.getRedelivered()) {
                    log.error("消息【{}】两次消费失败，不再放回队列", deliveryTag);
                    channel.basicReject(deliveryTag, false);
                } else {
                    log.error("消息【{}】第一次消费失败，放回队列，进行重试", deliveryTag);
                    channel.basicReject(deliveryTag, true);
                }
            } catch (IOException ex) {
                log.error("请查看RabbitMQ服务器是否出问题");
            }
        }
        Long userId = Long.valueOf(msg);
        UserAccount account = userAccountService.getOne(new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUserId, userId));
        if (account != null) {
            return ;
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        BigDecimal init = new BigDecimal(0);
        userAccount.setTotalAmount(init);
        userAccount.setLockAmount(init);
        userAccount.setAvailableAmount(init);
        userAccount.setTotalIncomeAmount(init);
        userAccount.setTotalPayAmount(init);
        userAccountService.save(userAccount);
    }
}
