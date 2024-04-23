package com.windowforsun.kafka.retrytopic.controller;

import com.windowforsun.kafka.retrytopic.domain.Item;
import com.windowforsun.kafka.retrytopic.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/demo/items")
public class ItemController {
    private final ItemRepository itemRepository;

    @GetMapping("/{itemId}/status")
    public ResponseEntity<String> getItemStatus(@PathVariable UUID itemId) {
        try {
            Optional<Item> item = this.itemRepository.findById(itemId);

            if(item.isPresent()) {
                return ResponseEntity.ok(item.get().getStatus().toString());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
