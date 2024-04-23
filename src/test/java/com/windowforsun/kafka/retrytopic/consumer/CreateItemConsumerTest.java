package com.windowforsun.kafka.retrytopic.consumer;

import com.windowforsun.kafka.retrytopic.event.CreateItem;
import com.windowforsun.kafka.retrytopic.mapper.JsonMapper;
import com.windowforsun.kafka.retrytopic.service.ItemService;
import com.windowforsun.kafka.retrytopic.util.TestEventData;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class CreateItemConsumerTest {
    private ItemService itemService;
    private CreateItemConsumer consumer;

    @BeforeEach
    public void setUp() {
        this.itemService = mock(ItemService.class);
        this.consumer = new CreateItemConsumer(this.itemService);
    }

    @Test
    public void testListen_Success() {
        CreateItem testEvent = TestEventData.buildCreateItemEvent(UUID.randomUUID(), RandomStringUtils.randomAlphabetic(8));
        String payload = JsonMapper.writeToJson(testEvent);

        this.consumer.listen(payload);

        verify(this.itemService, times(1)).createItem(testEvent);
    }

    @Test
    public void testListen_ServiceThrowsException() {
        CreateItem testEvent = TestEventData.buildCreateItemEvent(UUID.randomUUID(), RandomStringUtils.randomAlphabetic(8));
        String payload = JsonMapper.writeToJson(testEvent);

        doThrow(new RuntimeException("Service failure")).when(this.itemService).createItem(testEvent);

        this.consumer.listen(payload);

        verify(this.itemService, times(1)).createItem(testEvent);
    }
}
