package com.windowforsun.kafka.retrytopic.service;

import com.windowforsun.kafka.retrytopic.domain.Item;
import com.windowforsun.kafka.retrytopic.event.CreateItem;
import com.windowforsun.kafka.retrytopic.event.UpdateItem;
import com.windowforsun.kafka.retrytopic.exception.RetryableMessagingException;
import com.windowforsun.kafka.retrytopic.repository.ItemRepository;
import com.windowforsun.kafka.retrytopic.util.TestEntityData;
import com.windowforsun.kafka.retrytopic.util.TestEventData;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ItemServiceTest {
    private ItemService service;
    private ItemRepository itemRepository;

    @BeforeEach
    public void setUp() {
        this.itemRepository = mock(ItemRepository.class);
        this.service = new ItemService(this.itemRepository);
    }

    @Test
    public void testCreateItem() {
        final String name = RandomStringUtils.randomAlphabetic(8);
        CreateItem testEvent = TestEventData.buildCreateItemEvent(UUID.randomUUID(), name);

        this.service.createItem(testEvent);

        verify(this.itemRepository, times(1)).save(argThat(s -> s.getName().equals(name)));
    }

    @Test
    public void testUpdateItem_ItemUpdated() {
        UUID itemId = UUID.randomUUID();
        Item item = TestEntityData.buildItem(itemId, "my-item");
        when(this.itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        UpdateItem testEvent = TestEventData.buildUpdateItemEvent(itemId, ItemStatus.ACTIVE);

        this.service.updateItem(testEvent);

        verify(this.itemRepository, times(1)).save(argThat(s -> s.getStatus().equals(ItemStatus.ACTIVE)));
    }

    @Test
    public void testUpdateItem_ItemRetried() {
        UUID itemId = UUID.randomUUID();
        when(this.itemRepository.findById(itemId)).thenReturn(Optional.empty());

        UpdateItem testEvent = TestEventData.buildUpdateItemEvent(itemId, ItemStatus.ACTIVE);

        Exception exception = Assertions.assertThrows(RetryableMessagingException.class, () -> this.service.updateItem(testEvent));

        assertThat(exception.getMessage(), is("Retry event."));
        verify(this.itemRepository, times(0)).save(any());
    }
}
