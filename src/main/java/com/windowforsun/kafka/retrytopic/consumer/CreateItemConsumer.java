package com.windowforsun.kafka.retrytopic.consumer;

import com.windowforsun.kafka.retrytopic.event.CreateItem;
import com.windowforsun.kafka.retrytopic.mapper.JsonMapper;
import com.windowforsun.kafka.retrytopic.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateItemConsumer {
    private final AtomicInteger counter = new AtomicInteger();
    private final ItemService itemService;

    @KafkaListener(topics = "#{'${demo.topics.itemCreateTopic}'}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(@Payload final String payload) {
        this.counter.getAndIncrement();
        log.info("Create Item Consumer: Received message [" +counter.get()+ "] - payload: " + payload);

        try {
            CreateItem event = JsonMapper.readFromJson(payload, CreateItem.class);
            this.itemService.createItem(event);
        } catch (Exception e) {
            log.error("Create item - error processing message: " + e.getMessage());
        }
    }
}
