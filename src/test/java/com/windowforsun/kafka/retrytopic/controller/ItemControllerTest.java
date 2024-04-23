package com.windowforsun.kafka.retrytopic.controller;

import com.windowforsun.kafka.retrytopic.domain.Item;
import com.windowforsun.kafka.retrytopic.repository.ItemRepository;
import com.windowforsun.kafka.retrytopic.util.TestEntityData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ItemControllerTest {
    private ItemRepository itemRepositoryMock;
    private ItemController controller;

    @BeforeEach
    public void setUp() {
        this.itemRepositoryMock = mock(ItemRepository.class);
        this.controller = new ItemController(this.itemRepositoryMock);
    }

    @Test
    public void testGetItem_Success() {
        UUID itemId = UUID.randomUUID();
        Item item = TestEntityData.buildItem(itemId, "my-item");
        when(this.itemRepositoryMock.findById(itemId)).thenReturn(Optional.of(item));

        ResponseEntity<String> response = this.controller.getItemStatus(itemId);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(item.getStatus().toString()));
    }

    @Test
    public void testGetItem_NotFound() {
        UUID itemId = UUID.randomUUID();
        when(this.itemRepositoryMock.findById(itemId)).thenReturn(Optional.empty());

        ResponseEntity<String> response = this.controller.getItemStatus(itemId);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void testListen_RepositoryThrowsException() {
        UUID itemId = UUID.randomUUID();
        when(this.itemRepositoryMock.findById(itemId)).thenThrow(new RuntimeException("failed"));

        ResponseEntity<String> response = this.controller.getItemStatus(itemId);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
