package com.windowforsun.kafka.retrytopic.consumer;

import com.windowforsun.kafka.retrytopic.event.UpdateItem;
import com.windowforsun.kafka.retrytopic.mapper.JsonMapper;
import com.windowforsun.kafka.retrytopic.service.ItemService;
import com.windowforsun.kafka.retrytopic.service.ItemServiceTest;
import com.windowforsun.kafka.retrytopic.service.ItemStatus;
import com.windowforsun.kafka.retrytopic.util.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class UpdateItemConsumerTest {
    private ItemService itemService;
    private UpdateItemConsumer consumer;

    @BeforeEach
    public void setUp() {
        this.itemService = mock(ItemService.class);
        this.consumer = new UpdateItemConsumer(this.itemService);
    }

    @Test
    public void testListen_Success() {
        UpdateItem testEvent = TestEventData.buildUpdateItemEvent(UUID.randomUUID(), ItemStatus.ACTIVE);
        String payload = JsonMapper.writeToJson(testEvent);

        this.consumer.listen(payload);

        verify(this.itemService, times(1)).updateItem(testEvent);
    }

    @Test
    public void testListen_ServiceThrowsException() {
        UpdateItem testEvent = TestEventData.buildUpdateItemEvent(UUID.randomUUID(), ItemStatus.ACTIVE);
        String payload = JsonMapper.writeToJson(testEvent);

        doThrow(new RuntimeException("Service failure")).when(this.itemService).updateItem(testEvent);

        this.consumer.listen(payload);

        verify(this.itemService, times(1)).updateItem(testEvent);
    }
}
