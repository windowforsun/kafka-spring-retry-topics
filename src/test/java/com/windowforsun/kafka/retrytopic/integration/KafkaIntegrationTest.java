package com.windowforsun.kafka.retrytopic.integration;

import com.windowforsun.kafka.retrytopic.DemoConfig;
import com.windowforsun.kafka.retrytopic.event.CreateItem;
import com.windowforsun.kafka.retrytopic.event.UpdateItem;
import com.windowforsun.kafka.retrytopic.mapper.JsonMapper;
import com.windowforsun.kafka.retrytopic.repository.ItemRepository;
import com.windowforsun.kafka.retrytopic.service.ItemStatus;
import com.windowforsun.kafka.retrytopic.util.TestEventData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = DemoConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(controlledShutdown = true, topics = {"create-item", "update-item", "update-item-retry"})
public class KafkaIntegrationTest {
    final static String CREATE_ITEM_TOPIC = "create-item";
    final static String UPDATE_ITEM_TOPIC = "update-item";

    @Autowired
    private TestKafkaClient testKafkaClient;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private KafkaListenerEndpointRegistry registry;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        this.itemRepository.deleteAll();

        this.registry.getListenerContainers()
                .stream()
                .forEach(container ->
                        ContainerTestUtils.waitForAssignment(container, this.embeddedKafkaBroker.getPartitionsPerTopic()));
    }

    @Test
    public void testCreateAndUpdateItems() {
        int totalMessages = 10;
        Set<UUID> itemIds = new HashSet<>();

        for (int i = 0; i < totalMessages; i++) {
            UUID itemId = UUID.randomUUID();
            CreateItem createEvent = TestEventData.buildCreateItemEvent(itemId, RandomStringUtils.randomAlphabetic(8));
            this.testKafkaClient.sendMessage(CREATE_ITEM_TOPIC, JsonMapper.writeToJson(createEvent));
            itemIds.add(itemId);
        }

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> this.itemRepository.findAll().size() == totalMessages);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> this.itemRepository.findAll().stream().allMatch(item -> item.getStatus().equals(ItemStatus.NEW)));

        itemIds.forEach(itemId -> {
            UpdateItem updateEvent = TestEventData.buildUpdateItemEvent(itemId, ItemStatus.ACTIVE);
            this.testKafkaClient.sendMessage(UPDATE_ITEM_TOPIC, JsonMapper.writeToJson(updateEvent));
        });
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> this.itemRepository.findAll().stream().allMatch(item -> item.getStatus().equals(ItemStatus.ACTIVE)));
    }

    @Test
    public void testUpdateBeforeCreate() throws Exception {
        int totalMessage = 10;
        Set<UUID> itemIds = new HashSet<>();

        for(int i = 0; i < totalMessage; i++) {
            UUID itemId = UUID.randomUUID();
            UpdateItem updateEvent = TestEventData.buildUpdateItemEvent(itemId, ItemStatus.ACTIVE);
            this.testKafkaClient.sendMessage(UPDATE_ITEM_TOPIC, JsonMapper.writeToJson(updateEvent));
            itemIds.add(itemId);
        }

        TimeUnit.SECONDS.sleep(5);

        assertThat(this.itemRepository.findAll().size(), is(0));

        itemIds.forEach(itemId -> {
            CreateItem createEvent = TestEventData.buildCreateItemEvent(itemId, RandomStringUtils.randomAlphabetic(8));
            this.testKafkaClient.sendMessage(CREATE_ITEM_TOPIC, JsonMapper.writeToJson(createEvent));
        });

        Awaitility.await().atMost(30, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> this.itemRepository.findAll().size() == totalMessage);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> this.itemRepository.findAll().stream().allMatch(item -> item.getStatus().equals(ItemStatus.ACTIVE)));
    }

    @Test
    public void testUpdateEventIdDiscarded() throws Exception {
        UUID itemId = UUID.randomUUID();

        UpdateItem updateEvent = TestEventData.buildUpdateItemEvent(itemId, ItemStatus.ACTIVE);
        this.testKafkaClient.sendMessage(UPDATE_ITEM_TOPIC, JsonMapper.writeToJson(updateEvent));

        TimeUnit.SECONDS.sleep(12);

        CreateItem createEvent = TestEventData.buildCreateItemEvent(itemId, RandomStringUtils.randomAlphabetic(8));
        this.testKafkaClient.sendMessage(CREATE_ITEM_TOPIC, JsonMapper.writeToJson(createEvent));

        Awaitility.await().atMost(30, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> this.itemRepository.findAll().size() == 1);

        TimeUnit.SECONDS.sleep(3);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    ResponseEntity<String> response = this.restTemplate.getForEntity("/v1/demo/items/" + itemId + "/status", String.class);
                    return response.getStatusCode() == HttpStatus.OK && response.getBody().equals("NEW");
                });
    }
}
