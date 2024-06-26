package com.windowforsun.kafka.retrytopic.consumer;

import com.windowforsun.kafka.retrytopic.event.UpdateItem;
import com.windowforsun.kafka.retrytopic.exception.RetryableMessagingException;
import com.windowforsun.kafka.retrytopic.mapper.JsonMapper;
import com.windowforsun.kafka.retrytopic.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateItemConsumer {
    private final ItemService itemService;

    @RetryableTopic(
            attempts = "#{'${demo.retry.maxRetryAttempts}'}",
            autoCreateTopics = "#{'${demo.retry.autoCreateRetryTopics}'}",
            backoff = @Backoff(delayExpression = "#{'${demo.retry.retryIntervalMilliseconds}'}",
                    multiplierExpression = "#{'${demo.retry.retryBackoffMultiplier}'}"),
            fixedDelayTopicStrategy = FixedDelayStrategy.MULTIPLE_TOPICS,
            include = {RetryableMessagingException.class},
            timeout = "#{'${demo.retry.maxRetryDurationMilliseconds}'}",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = "#{'${demo.topics.itemUpdateTopic}'}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(@Payload final String payload) {
        log.info("Update Item Consumer: Received message with payload: " + payload);

        try {
            UpdateItem event = JsonMapper.readFromJson(payload, UpdateItem.class);
            this.itemService.updateItem(event);
        } catch (RetryableMessagingException e) {
            // Ensure the message is retried.
            log.error("Retrying message : {}", payload);
            throw e;
        } catch (Exception e) {
            log.error("Update item - error processing message: " + e.getMessage());
        }
    }

    @DltHandler
    public void dlt(String data, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Event from topic "+topic+" is dead lettered - event:" + data);
    }
}
