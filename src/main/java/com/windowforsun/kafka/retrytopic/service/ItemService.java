package com.windowforsun.kafka.retrytopic.service;

import com.windowforsun.kafka.retrytopic.domain.Item;
import com.windowforsun.kafka.retrytopic.event.CreateItem;
import com.windowforsun.kafka.retrytopic.event.UpdateItem;
import com.windowforsun.kafka.retrytopic.exception.RetryableMessagingException;
import com.windowforsun.kafka.retrytopic.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public void createItem(final CreateItem event) {
        Item item = Item.builder()
                .id(event.getId())
                .name(event.getName())
                .status(ItemStatus.NEW)
                .build();
        this.itemRepository.save(item);
        log.info("Item persisted to database with Id: {}", event.getId());
    }

    public void updateItem(final UpdateItem event) {
        final Optional<Item> item = this.itemRepository.findById(event.getId());

        if(item.isPresent()) {
            item.get().setStatus(event.getStatus());
            this.itemRepository.save(item.get());
            log.info("Item updated in database with Id: {}", event.getId());
        } else {
            // Retry
            log.info("Item not found with Id: {} - retrying update event", event.getId());
            throw new RetryableMessagingException("Retry event.");
        }
    }
}
