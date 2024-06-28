package com.atguigu.tingshu.search;

import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.search.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AlbumUpOrDownConsumer {

    @Autowired
    private ItemService itemService;

    @KafkaListener(topics = KafkaConstant.QUEUE_ALBUM_UPPER)
    public void albumUpConsumer(ConsumerRecord<String, String> consumerRecord) {
        log.info("kafka收到消息：{}", consumerRecord.key());
        itemService.addAlbumFromDbToEs(Long.valueOf(consumerRecord.value()));
    }

    @KafkaListener(topics = KafkaConstant.QUEUE_ALBUM_LOWER)
    public void albumDownConsumer(ConsumerRecord<String, String> consumerRecord) {
        log.info("kafka收到消息：{}", consumerRecord.key());
        itemService.removeAlbumFromEs(Long.valueOf(consumerRecord.value()));
    }
}
