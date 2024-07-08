package com.atguigu.tingshu.album.consumer;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class AlbumFanoutConsumer {

    @Autowired
    private AlbumInfoService albumInfoService;

    @RabbitListener(queues = "album_fanout_queue")
    public void albumFanoutConsumer(Channel channel, Message message) {
        MessageProperties messageProperties = message.getMessageProperties();
        // 消息编号
        long deliveryTag = messageProperties.getDeliveryTag();
        String orderNo = new String(message.getBody());
        try {
            // 确认消息，false=只确认当前的消息
            albumInfoService.updateAlbumStat(orderNo);
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
    }
}
